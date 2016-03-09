package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.game.GameWorld;

public class Banner extends RenderObject implements Constants {
  protected int _carIndex;
  protected String [] _names = new String[]{"Orange", "Green", "Red", "Blue"};
  
  public Banner(GameWorld w, int carIndex) {
    super(w);
    _carIndex = carIndex;
    _position = new Vector2(GAME_WIDTH + 10, w.getGameHeight() / 2 + 50);
    _acceleration = new Vector2(50, 0);
    _velocity = new Vector2(-500, 0);
  }

  @Override
  protected void updateObject(float delta) {
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
    GlyphLayout gl = r.drawText(_names[_carIndex] + " car wins!", _position.x, _position.y);
    if(_position.x <= ((GAME_WIDTH / 2) - (gl.width / 2))) {
      _acceleration = new Vector2(0, 0);
      _velocity = new Vector2(0, 0);
    }
  }
}
