package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.intel.amf.dice.screens.ShapeObject;
import com.intel.amf.dice.screens.game.GameWorld;

public class StartBanner extends ShapeObject {
  protected float _counter;
  protected boolean _go;
  protected GameWorld _w;

  public StartBanner(GameWorld w, int x, int y, int width, int height) {
    super(w, x, y, width, height);
    _w = w;
    _counter = 0;
    _color = Color.BLACK.cpy();
    _color.a = 0.7f;
  }
  
  public void go() {
    _go = true;
  }
  
  @Override
  protected void updateObject(float delta) {
    _counter += delta;
    if(_counter > 0.01) {
      _counter = 0;
      
      if(_go) {
        if(_color.a >= 0.0f) {
          _color.a -= 0.1f;
          
          if(_color.a <= 0) {
            _live = false;
            _w.beginGame();
          }
        }
      }
    }
  }
}
