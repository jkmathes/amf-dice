package com.intel.amf.dice;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Networking and game coordination between the game
 * itself and the game server
 * 
 * @author jkmathes
 */
public class Orchestration {
  /**
   * Whether or not there is a pending long poll
   */
  protected boolean _pending;
  /**
   * The host of the game server
   */
  protected String _url;
  /**
   * The current result of the long poll
   */
  protected String _msg;
  
  /**
   * Create an orchestration between this game
   * and the game server
   * 
   * @param url the game server host to use
   */
  public Orchestration(String url) {
    _url = url;
    _msg = null;
    _pending = false;
  }
     
  /**
   * Determine if work is ready for processing from the game server
   * 
   * @return any work which needs to be processed
   */
  public boolean hasWork() {
    if(_msg != null) {
      return true;
    }
    if(_pending == false) {
      fetchWork();
    }
    return false;
  }
  
  /**
   * Get the currently available piece of work
   * @return
   */
  public String getWork() {
    String tmp = _msg;
    _msg = null;
    return tmp;
  }
  
  private void fetchWork() {
    HttpRequest get = new HttpRequest(HttpMethods.GET);
    get.setUrl(_url + "/work");
    _pending = true;
    
    Gdx.net.sendHttpRequest(get, new HttpResponseListener() {

      @Override
      public void handleHttpResponse(HttpResponse response) {
        _msg = response.getResultAsString();
        _pending = false;
      }

      @Override
      public void failed(Throwable t) {
        _msg = null;
        _pending = false;
      }

      @Override
      public void cancelled() {
        _pending = false;
      }
    });
  }
  
  public void sendWin(int car) {
    sendCommand("{\"type\": \"win\", \"data\": {\"car\": \"" + car + "\"}}");
  }
  
  private void sendCommand(String msg) {
    HttpRequest post = new HttpRequest(HttpMethods.POST);
    post.setUrl(_url + "/event");
    post.setHeader("Content-Type", "application/json");
    post.setContent(msg);
    Gdx.net.sendHttpRequest(post, new HttpResponseListener() {

      @Override
      public void handleHttpResponse(HttpResponse httpResponse) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void failed(Throwable t) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void cancelled() {
        // TODO Auto-generated method stub
        
      }
    });
  }
  
  public static interface WorkHandler {
    public void handle(JsonValue j);
  }
}
