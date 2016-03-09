package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.intel.amf.dice.screens.ShapeObject;
import com.intel.amf.dice.screens.World;

public class Overlay extends ShapeObject {
  protected float _counter;
  
  public Overlay(World w, int x, int y, int width, int height) {
    super(w, x, y, width, height);
    _counter = 0;
    _color = Color.BLACK.cpy();
    _color.a = 0.0f;
  }
  
  @Override
  protected void updateObject(float delta) {
    _counter += delta;
    if(_counter > 0.01) {
      _counter = 0;
      
      if(_color.a <= 0.7f) {
        _color.a += 0.01f;
      }
    }
  }
}
