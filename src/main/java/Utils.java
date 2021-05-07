import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

public class Utils {

  public static final ObjectMapper objectMapper = new ObjectMapper()
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static final BasicCookieStore cookieStore = new BasicCookieStore();

  public static final HttpClient httpClient = HttpClientBuilder.create()
      .setDefaultCookieStore(cookieStore)
      .setDefaultRequestConfig(RequestConfig.custom()
          .setConnectTimeout(4000)
          .setSocketTimeout(4000)
          .setConnectionRequestTimeout(3000)
          .build()
      ).build();


}
