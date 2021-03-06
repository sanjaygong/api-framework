package in.erail.model;

import org.junit.Assert;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author vinay
 */
public class ResponseEventTest {

  
  public void testMapping() {
    JsonObject result = new JsonObject("{\n"
            + "  \"body\" : \"VGVzdGluZw==\",\n"
            + "  \"isBase64Encoded\" : true,\n"
            + "  \"statusCode\" : 200,\n"
            + "  \"multiValueHeaders\":{},\n"
            + "  \"cookies\" : [{\n"
            + "    \"name\" : \"yello\"\n"
            + "    \"value\" : \"yello\"\n"
            + "    \"httpOnly\" : \"true\"\n"
            + "    \"secure\" : \"true\"\n"
            + "    \"path\" : \"/fsdfsd\"\n"
            + "    \"maxAge\" : \"233\"\n"
            + "    \"domain\" : \"yahoo.com\"\n"
            + "  }],\n"
            + "  \"headers\" : {\n"
            + "    \"a\" : \"b\"\n"
            + "  }\n"
            + "}");

    ResponseEvent response = new ResponseEvent();

    response.setBody("Testing".getBytes());
    response.addHeader("a", "b");

    JsonObject json = JsonObject.mapFrom(response);
    Assert.assertEquals(result, json);
  }

}
