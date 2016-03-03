package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.game.GameWorld;

public class Car extends RenderObject {
  protected float _rot;
  protected int _pathIndex;
  protected int _lastX;
  protected int _lastY;
  protected float _counter;
  protected int _targetPathIndex;
  protected boolean _go;
  protected int _carIndex;
  protected GameWorld _w;
  public static int [] PATH_OFFSETS = new int[]{-30, -15, 0, 15};
  
  public Car(GameWorld w, int x, int y, int width, int height, int carIndex) {
    super(w);
    _w = w;
    _position.x = x;
    _position.y = y;
    _width = width;
    _height = height;
    _pathIndex = 0;
    _counter = 0;
    _lastX = x - 16;
    _lastY = y;
    _go = false;
    _carIndex = carIndex;
  }

  @Override
  protected void updateObject(float delta) {    
    _counter += delta;
    if(_counter < 0.015) {
      return;
    }
    
    _counter = 0;
    
    if(_go && _pathIndex < _targetPathIndex) {
      _pathIndex++;
    }
    
    if(_pathIndex > ((GameWorld)_world).getPath().size() - 1) {
      _pathIndex = 0;
    }
    _position = ((GameWorld)_world).getPath().get(_pathIndex);
    
    int deltaX = (int)_position.x - _lastX;
    int deltaY = (int)_position.y - _lastY;
    
    if(deltaX > 0) {
      _rot = 0;
      if(deltaY < 0) {
        _rot -= 45;
      }
      else if(deltaY > 0) {
        _rot += 45;
      }
    }
    else if(deltaX < 0) {
      _rot = 180;
      if(deltaY < 0) {
        _rot += 45;
      }
      else if(deltaY > 0) {
        _rot -= 45;
      }
    }
    else {
      if(deltaY < 0) {
        _rot = 270;
      }
      else if(deltaY > 0) {
        _rot = 90;
      }
    } 
    
    _lastX = (int)_position.x;
    _lastY = (int)_position.y;
  }

  public void incrementPathIndex(int inc) {
    _targetPathIndex = _pathIndex + inc;
  }
  
  public boolean inMotion() {
    if(_pathIndex < _targetPathIndex) {
      return true;
    }
    _go = false;
    return false;
  }
  
  public void go() {
    _go = true;
  }
  
  @Override
  public void render(SpriteBatch sb, Renderer r) {
    sb.draw(AssetLoader._cars[_carIndex], getX() - (getWidth() / 2), getY() - (getHeight() / 2), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), 1.0f, 1.0f, _rot);
  }
}
