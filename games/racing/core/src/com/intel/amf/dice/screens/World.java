package com.intel.amf.dice.screens;

import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;

public class World {
  protected RenderObject _grabbed;
  protected LinkedList<float []> _flingQueue;
  
  public World() {
    _flingQueue = new LinkedList<float []>();
  }

  public void grab(float x, float y) {
    if(_grabbed != null) {
      return;
    }
    _flingQueue.add(new float[]{x, y, 0f, 0f});
  }

  public void drag(float x, float y) {
    if(_grabbed == null) {
      return;
    }
    _grabbed.setX(x - (_grabbed.getWidth() / 2));
    _grabbed.setY(y - (_grabbed.getHeight() / 2));
  }

  public void fling(float x, float y, float velocityX, float velocityY) {
    if(_grabbed == null) {
      return;
    }
    _grabbed.setVelocity(new Vector2(velocityX, velocityY));
    _grabbed.setFlung(true);
    _grabbed = null;
  }
}