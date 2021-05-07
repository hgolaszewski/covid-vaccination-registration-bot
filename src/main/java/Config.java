import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Appointment;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import job.Job;
import job.JobList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Config {

  private static final Config instance = new Config();

  private String prescriptionId;
  private String userSystemId;
  private String sessionId;
  private String xcsrfToken;

  private String urlUserProfile;
  private String urlAppointmentList;
  private String urlAppointmentCancel;
  private String urlAppointmentRegister;

  private Boolean registerWhenAppointmentFound;
  private Boolean registerOnlyIfDateIsBefore;

  private List<Job> jobs;

  @Setter
  private Appointment confirmedAppointment;

  private final Map<String, String> requestHeaders;

  private Config() {
    loadUserProperties();
    loadJobs();
    requestHeaders = Map.of(
        "x-csrf-token", xcsrfToken,
        "content-type", "text/plain;charset=UTF-8",
        "accept", "application/json, text/plain, */*",
        "origin", "https://pacjent.erejestracja.ezdrowie.gov.pl",
        "user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36"
    );
  }

  private void loadUserProperties() {

    log.info("Loading auth properties...");

    try (InputStream inputStream = BotStarter.class.getResourceAsStream("config.properties")) {

      Properties props = new Properties();
      props.load(inputStream);

      prescriptionId = props.getProperty("prescription_id");
      userSystemId = props.getProperty("user_id");
      sessionId = props.getProperty("session_id");
      xcsrfToken = props.getProperty("xcsrf_token");
      registerWhenAppointmentFound = parseBoolean(props.getProperty("register"));
      registerOnlyIfDateIsBefore = parseBoolean(props.getProperty("register_only_if_date_is_before"));

      urlUserProfile = props.getProperty("url_user_profile");
      urlAppointmentList = props.getProperty("url_appointment_list");
      urlAppointmentCancel = props.getProperty("url_appointment_cancel");
      urlAppointmentRegister = props.getProperty("url_appointment_register");

      log.info("User prescription id : {}", prescriptionId);
      log.info("User id : {}", userSystemId);
      log.info("User session id : {}", sessionId);
      log.info("User xcsrf token : {}", xcsrfToken);
      log.info("Register when appointment found? : {}", registerWhenAppointmentFound);
      log.info("Register only when date is before? : {}", registerOnlyIfDateIsBefore);

      log.info("URL user profile : {}", urlUserProfile);
      log.info("URL appointments list : {}", urlAppointmentList);
      log.info("URL appointment cancel : {}", urlAppointmentCancel);
      log.info("URL appointment register : {}", urlAppointmentRegister);

    } catch (Exception ex) {
      log.error("Failed to load user properties", ex);
    }

  }

  private void loadJobs() {

    log.info("Loading jobs list...");

    try (InputStream inputStream = BotStarter.class.getResourceAsStream("jobs.json")) {

      ObjectMapper objectMapper = new ObjectMapper();
      JobList joblist = objectMapper.readValue(inputStream, JobList.class);
      jobs = joblist.getJobs();

    } catch (Exception ex) {
      log.error("Failed to load jobs list", ex);
    }

  }

  public static Config config() {
    return instance;
  }

}
