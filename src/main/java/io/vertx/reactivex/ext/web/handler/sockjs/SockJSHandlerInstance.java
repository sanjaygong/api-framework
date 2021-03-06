package io.vertx.reactivex.ext.web.handler.sockjs;

import in.erail.glue.annotation.StartService;
import io.vertx.core.Handler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.Transport;
import io.vertx.reactivex.core.Vertx;

/**
 *
 * @author vinay
 */
public class SockJSHandlerInstance {

  private Vertx mVertx;
  private BridgeOptions mBridgeOptions;
  private Handler<BridgeEvent> mBridgeEventHandler;
  private SockJSHandler mSockJSHandler;

  @StartService
  public void start() {
    
    // Eventbus handle
    SockJSHandlerOptions sockJSHandlerOptions = (new SockJSHandlerOptions())
            .addDisabledTransport(Transport.EVENT_SOURCE.toString())
            .addDisabledTransport(Transport.HTML_FILE.toString())
            .addDisabledTransport(Transport.JSON_P.toString())
            .addDisabledTransport(Transport.XHR.toString())
            .setInsertJSESSIONID(false);

     mSockJSHandler = SockJSHandler
            .create(getVertx(), sockJSHandlerOptions)
            .bridge(getBridgeOptions(), getBridgeEventHandler());
  }

  public Vertx getVertx() {
    return mVertx;
  }

  public void setVertx(Vertx pVertx) {
    this.mVertx = pVertx;
  }

  public BridgeOptions getBridgeOptions() {
    return mBridgeOptions;
  }

  public void setBridgeOptions(BridgeOptions pBridgeOptions) {
    this.mBridgeOptions = pBridgeOptions;
  }

  public Handler<BridgeEvent> getBridgeEventHandler() {
    return mBridgeEventHandler;
  }

  public void setBridgeEventHandler(Handler<BridgeEvent> pBridgeEventHandler) {
    this.mBridgeEventHandler = pBridgeEventHandler;
  }

  public SockJSHandler create() {
    return mSockJSHandler;
  }

}
