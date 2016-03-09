package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.game.GameWorld;

public class Countdown extends RenderObject implements Constants {
  protected int _value;
  protected boolean _chimed;
  protected GlyphLayout _gl;
  protected GameWorld _w;

  public Countdown(GameWorld w, int value) {
    super(w);
    _w = w;
    _value = value;
    _position = new Vector2(GAME_WIDTH + 10, 128 * 5 - 30);
    _velocity = new Vector2(-2000, 0);
  }
  
  @Override
  protected void updateObject(float delta) {
    if(!_chimed && _position.x <= ((GAME_WIDTH / 2) - (_gl.width / 2))) {
      _chimed = true;
      if(_value == 0) {
        AssetLoader._go.play();
        _w.getStartBanner().go();
      }
      else {
        AssetLoader._signal.play();
      }
      _velocity = new Vector2(-50, -50);
    }
    
    if(_chimed) {
      Color c = AssetLoader._font.getColor();
      c.a -= 0.02f;
      if(c.a <= 0) {
        _live = false;
        c.a = 1.0f;
        if(_value >= 1) {
          _w.addObject(new Countdown(_w, _value - 1));
        }
      }
    }
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
    if(_value == 0) {
      _gl = r.drawText("GO", _position.x, _position.y);
    }
    else {
      _gl = r.drawText("" + _value, _position.x, _position.y);
    }
  }
}
