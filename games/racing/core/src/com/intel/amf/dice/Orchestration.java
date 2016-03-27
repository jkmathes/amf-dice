package com.intel.amf.dice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Networking and game coordination between the game
 * itself and the game server
 * 
 * @author jkmathes
 */
public class Orchestration {
  /**
   * The host of the game server
   */
  protected String _url;
  /**
   * A thread to fetch messages asynchronously
   */
  protected Thread _workThread;
  /**
   * The work socket in use
   */
  protected Socket _socket;
  /**
   * A queue of work coming from the server
   */
  protected ConcurrentLinkedQueue<String> _messages;
  
  /**
   * Create an orchestration between this game
   * and the game server
   * 
   * @param url the game server host to use
   */
  public Orchestration(final String url) {
    _url = url;
    _messages = new ConcurrentLinkedQueue<String>();
    _workThread = new Thread(new Runnable() {

      @Override
      public void run() {
        boolean reconnect = true;
        while(true) {
          try {
            if(reconnect) {
              reconnect = false;
              SocketHints hints = new SocketHints();
              hints.keepAlive = true;
              hints.connectTimeout = 5000;
              _socket = Gdx.net.newClientSocket(Protocol.TCP, url, 8000, hints);
            }
            
            if(_socket.isConnected()) {
              String msg = new BufferedReader(new InputStreamReader(_socket.getInputStream())).readLine();
              if(msg != null) {
                _messages.add(msg);
              }
              else {
                throw new GdxRuntimeException("null msg received, socket issue most likely");
              }
            }
          }
          catch(IOException | GdxRuntimeException e) {
            Gdx.app.error("AMF-Racing", "Orchestration problem - reconnecting in 5s", e);
            reconnect = true;
            try {
              Thread.sleep(5000);
            }
            catch(InterruptedException ie) {
              ie.printStackTrace();
            }
          }
        }
      }
    });
    _workThread.start();
  }
     
  /**
   * Determine if work is ready for processing from the game server
   * 
   * @return any work which needs to be processed
   */
  public boolean hasWork() {
    return _messages.peek() != null;
  }
  
  public void sendWin(int car) {
    sendCommand("{\"type\": \"win\", \"data\": {\"car\": \"" + car + "\"}}");
  }
  
  private void sendCommand(String msg) {
    if(_socket.isConnected()) {
      try {
        _socket.getOutputStream().write((msg + "\n").getBytes());
      }
      catch(IOException e) {
        /**
         * If we try to send a command and the server isn't available, should we queue and try again?
         */
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Get the currently available piece of work
   * @return the work to be done, if available, null otherwise
   */
  public String getWork() {
    return _messages.poll();
  }
    
  public static interface WorkHandler {
    public void handle(JsonValue j);
  }
}
