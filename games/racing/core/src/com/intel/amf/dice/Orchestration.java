package com.intel.amf.dice;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;

public class Orchestration {
  protected boolean _pending;
  protected String _url;
  protected String _msg;
  
  public Orchestration(String url) {
    _url = url;
    _msg = null;
    _pending = false;
  }
     
  public boolean hasWork() {
    if(_msg != null) {
      return true;
    }
    if(_pending == false) {
      fetchWork();
    }
    return false;
  }
  
  public String getWork() {
    String tmp = _msg;
    _msg = null;
    return tmp;
  }
  
  private void fetchWork() {
    HttpRequest get = new HttpRequest(HttpMethods.GET);
    get.setUrl("http://localhost:3000");
    _pending = true;
    
    Gdx.net.sendHttpRequest(get, new HttpResponseListener() {

      @Override
      public void handleHttpResponse(HttpResponse response) {
        _msg = response.getResultAsString();
        _pending = false;
      }

      @Override
      public void failed(Throwable t) {
        _pending = false;
      }

      @Override
      public void cancelled() {
        _pending = false;
      }
    });
  }
  
  public static interface WorkHandler {
    public void handle(JSONObject j);
  }
}
