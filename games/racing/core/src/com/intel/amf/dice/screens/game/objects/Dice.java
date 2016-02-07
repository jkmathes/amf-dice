package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.World;

public class Dice extends RenderObject {
  protected int _value;
  public int _targetx;
  public int _targety;
  
  protected float _incx;
  protected float _incy;
  
  public Dice(World w, int value) {
    super(w);
    _value = value;
  }

  @Override
  protected void updateObject(float delta) {
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
    sb.draw(AssetLoader._diceWhite[_value - 1], _position.x - 18, _position.y - 18);
  }
  
  public void setTargetPosition(float x, float y) {
    if(x > _position.x) {
      _velocity.x = 600;
    }
    else {
      _velocity.x = -600;
    }
    
    if(y > _position.y) {
      _velocity.y = 600;
      _acceleration.y = -800;
    }
    else {
      _velocity.y = -600;
      _acceleration.y = 800;
    }
  }
}
