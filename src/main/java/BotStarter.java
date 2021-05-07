import static java.util.concurrent.TimeUnit.MILLISECONDS;

import dto.Appointment;
import dto.UserDetails;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.cookie.BasicClientCookie;

@Slf4j
public class BotStarter {

  private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public static void main(String[] args) {

    log.info("Starting bot...");

    loadCookies();
    loadConfirmedAppointmentIfPresent();
    askForActionIfAppointmentIsAlreadyConfirmed();

    // Jobs will be executed sequentially, due to single threaded executor
    Config.config().getJobs().forEach(job -> executorService.scheduleWithFixedDelay(
        new JobExecutor(job), 0, 1, MILLISECONDS
    ));

  }

  private static void askForActionIfAppointmentIsAlreadyConfirmed() {
    if (Config.config().getConfirmedAppointment() != null && Config.config().getRegisterWhenAppointmentFound()) {
      System.out.println("Found confirmed appointment. Registration function is enabled (config.properties).");
      System.out.println("Type 'y' to continue (current appointment could be cancelled and overridden. Type whatever to exit.");
      String answer = new Scanner(System.in).nextLine();
      if (!answer.equals("y")) {
        System.exit(1);
      }
    }
  }

  private static void loadCookies() {
    BasicClientCookie cookie = new BasicClientCookie("patient_sid", Config.config().getSessionId());
    cookie.setDomain("pacjent.erejestracja.ezdrowie.gov.pl");
    cookie.setPath("/api");
    Utils.cookieStore.addCookie(cookie);

  }

  private static void loadConfirmedAppointmentIfPresent() {

    try {

      HttpGet request = new HttpGet(String.format(
          Config.config().getUrlUserProfile(),
          Config.config().getUserSystemId()
      ));
      Config.config().getRequestHeaders().forEach(request::addHeader);

      HttpResponse response = Utils.httpClient.execute(request);

      UserDetails userDetails = Utils.objectMapper.readValue(
          response.getEntity().getContent(), UserDetails.class
      );

      Appointment appointment = userDetails.getAppointments().get(0);

      if (appointment != null) {
        log.info("Found already confirmed appointment: {}", appointment);
        Config.config().setConfirmedAppointment(appointment);
      }

    } catch (IOException ex) {
      log.error("Failed to load confirmed appointments", ex);
    }

  }


}
