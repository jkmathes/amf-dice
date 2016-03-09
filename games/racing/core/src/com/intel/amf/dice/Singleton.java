package com.intel.amf.dice;

/**
 * Small singleton to facilitate global settings between
 * the native driver and the game core
 * 
 * @author jkmathes
 */
public class Singleton {
  /**
   * The single instance
   */
  private static Singleton _instance;
  
  /**
   * The URL which hosts the game server
   */
  protected String _gameHost;
  protected AMFRacing _game;
  
  protected Singleton() {
  }
  
  /**
   * Get the single instance - lazy instantiation
   * @return the singleton instance
   */
  public static Singleton getInstance() {
    if(_instance == null) {
      synchronized(Singleton.class) {
        if(_instance == null) {
          _instance = new Singleton();
        }
      }
    }
    return _instance;
  }
  
  /**
   * Get the host or IP of the game server
   * @return the game server host or IP
   */
  public String getGameHost() {
    return _gameHost;
  }
  
  /**
   * Set the host for the game server
   * @param url the game server host to set
   */
  public void setGameHost(String url) {
    _gameHost = url;
  }
  
  public AMFRacing getGame() {
    return _game;
  }
  
  public void setGame(AMFRacing g) {
    _game = g;
  }
}
