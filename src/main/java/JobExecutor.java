import static java.util.Comparator.comparingInt;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import dto.Appointment;
import dto.AppointmentList;
import java.io.IOException;
import java.util.Optional;
import job.Job;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

@Slf4j
public class JobExecutor implements Runnable {

  private final Job job;
  private boolean notRegistered = true;

  public JobExecutor(Job job) {
    this.job = job;
  }

  @Override
  public void run() {

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (notRegistered) {

      log.info("Searching for available appointment dates with params: {}", job.getPayload());

      getAppointmentsList()
          .flatMap(this::getMatchingAppointment)
          .ifPresent(this::cancelCurrentAndRegisterNewAppointment);

    }

  }

  private Optional<AppointmentList> getAppointmentsList() {

    try {

      HttpPost listingRequest = new HttpPost(Config.config().getUrlAppointmentList());
      Config.config().getRequestHeaders().forEach(listingRequest::addHeader);

      StringEntity listingRequestBody = new StringEntity(
          String.format(job.getPayload(), Config.config().getPrescriptionId()),
          APPLICATION_JSON
      );
      listingRequest.setEntity(listingRequestBody);

      HttpResponse listingResponse = Utils.httpClient.execute(listingRequest);
      if (listingResponse.getStatusLine().getStatusCode() != 200) {
        log.warn("Response code for listing: {}", listingResponse.getStatusLine().getStatusCode());
      }

      AppointmentList appointmentList = Utils.objectMapper.readValue(
          listingResponse.getEntity().getContent(), AppointmentList.class
      );

      return Optional.of(appointmentList);

    } catch (Exception e) {
      log.error("Failed to load appointments list", e);
    }

    return Optional.empty();

  }

  private Optional<Appointment> getMatchingAppointment(AppointmentList appointmentList) {

    return appointmentList.getList().stream()
        .filter(appointment -> appointment.getStatus().toLowerCase().contains("active"))
        .filter(appointment -> appointment.getServicePoint().getAddressText().toLowerCase().contains(job.getCity()))
        .filter(appointment -> {
          if (Config.config().getConfirmedAppointment() != null && Config.config().getRegisterOnlyIfDateIsBefore()) {
            // If found appointment start time is before already confirmed, then take it into consideration
            return appointment.getStartAt().before(Config.config().getConfirmedAppointment().getStartAt());
          } else {
            // If there is no already confirmed appointment, then take it into consideration
            return true;
          }
        })
        .filter(CustomFilter.YOUR_FILTER)
        .peek(appointment -> log.info("Found appointment: {} ", appointment))
        .min(comparingInt(appointment -> (int) appointment.getStartAt().toInstant().toEpochMilli()));
  }

  private void cancelCurrentAndRegisterNewAppointment(Appointment matchingAppointment) {
    try {

      if (Config.config().getRegisterWhenAppointmentFound() &&
          !matchingAppointment.equals(Config.config().getConfirmedAppointment())) {

        // If there is already confirmed appointment, cancel it, because another with better start time was found
        if (Config.config().getConfirmedAppointment() != null) {
          cancelConfirmedAppointment();
        }

        HttpPost registrationRequest = new HttpPost(String.format(
            Config.config().getUrlAppointmentRegister(),
            matchingAppointment.getId()
        ));
        Config.config().getRequestHeaders().forEach(registrationRequest::addHeader);

        StringEntity registerRequestBody = new StringEntity(
            String.format("{\"prescriptionId\":\"%s\"}", Config.config().getPrescriptionId()), APPLICATION_JSON
        );
        registrationRequest.setEntity(registerRequestBody);

        HttpResponse registerResponse = Utils.httpClient.execute(registrationRequest);

        // TODO - make sure that appointment was in fact confirmed (failed requests also returns 200 OK)

        if (registerResponse.getStatusLine().getStatusCode() != 200) {
          log.warn("Response code for registration: {}", registerResponse.getStatusLine().getStatusCode());
        }

        log.info("Appointment confirmed: {}", matchingAppointment.getId());

        Config.config().setConfirmedAppointment(matchingAppointment);

        notRegistered = false;

      }
    } catch (Exception e) {
      log.error("Failed to cancel / register appointment");
    }

  }

  private void cancelConfirmedAppointment() throws IOException, InterruptedException {

    Appointment appointmentToCancel = Config.config().getConfirmedAppointment();

    HttpDelete deleteRequest = new HttpDelete(String.format(
        Config.config().getUrlAppointmentCancel(), appointmentToCancel.getId())
    );
    Config.config().getRequestHeaders().forEach(deleteRequest::addHeader);

    Thread.sleep(1000);

    HttpResponse deleteResponse = Utils.httpClient.execute(deleteRequest);

    // TODO - make sure that appointment was in fact cancelled (failed requests also returns 200 OK)

    if (deleteResponse.getStatusLine().getStatusCode() != 200) {
      log.warn("Response code for cancelling: {}", deleteResponse.getStatusLine().getStatusCode());
    }

    log.info("Appointment cancelled: {}", appointmentToCancel);

    Config.config().setConfirmedAppointment(null);
  }

}
