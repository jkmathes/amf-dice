package com.intel.amf.dice.screens;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class Gestures implements GestureDetector.GestureListener {
  protected World _w;
  protected float _scaleX;
  protected float _scaleY;
  protected float _x;
  protected float _y;

  public Gestures(World w, float scaleX, float scaleY) {
    _w = w;
    _scaleX = scaleX;
    _scaleY = scaleY;
  }

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    _x = scaleX(x);
    _y = scaleY(y);
    _w.grab(_x, _y);
    return true;
  }

  @Override
  public boolean tap(float x, float y, int count, int button) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    _w.fling(_x, _y, scaleX(velocityX), scaleY(velocityY));
    return true;
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    _x = scaleX(x);
    _y = scaleY(y);
    _w.drag(scaleX(x), scaleY(y));
    return true;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, int button) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
    // TODO Auto-generated method stub
    return false;
  }

  protected float scaleX(float screenX) {
    return (screenX / _scaleX);
  }

  protected float scaleY(float screenY) {
    return (screenY / _scaleY);
  }
}