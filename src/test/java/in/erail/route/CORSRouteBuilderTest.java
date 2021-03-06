package in.erail.route;

import com.google.common.net.HttpHeaders;
import in.erail.server.Server;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import in.erail.glue.Glue;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 *
 * @author vinay
 */
@RunWith(VertxUnitRunner.class)
public class CORSRouteBuilderTest {

  @Rule
  public Timeout rule = Timeout.seconds(2000);

  @SuppressWarnings("deprecation")
  @Test
  public void testProcess(TestContext context) {

    Async async = context.async();

    Server server = Glue.instance().resolve("/in/erail/server/Server");
    
    server
            .getVertx()
            .createHttpClient()
            .options(server.getHttpServerOptions().getPort(), server.getHttpServerOptions().getHost(), "/v1/broadcast/testTopic")
            .putHeader("content-type", "application/json")
            .putHeader(HttpHeaders.ORIGIN, "https://test.com")
            .putHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            .putHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "cache-control,content-type,postman-token")
            .handler(response -> {
              context.assertEquals(response.statusCode(), 200, response.statusMessage());
              context.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString()), "DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT");
              context.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()), "*");
              context.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString()), "3600");
              context.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString()), "X-POST");
              async.complete();
            })
            .end();

  }

}
