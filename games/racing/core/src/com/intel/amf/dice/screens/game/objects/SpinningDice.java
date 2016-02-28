package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.World;

public class SpinningDice extends RenderObject {
  private SpinningDice.Color _c;
  private int _value;
  private boolean _spinning;
  
  public static enum Color { BLUE, WHITE };
  
  public SpinningDice(World w, SpinningDice.Color c) {
    super(w);
    _spinning = true;
    _c = c;
  }

  @Override
  protected void updateObject(float delta) {
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
    if(_spinning) {
      return;
    }
    if(_c == Color.BLUE) {
      sb.draw(AssetLoader._diceBlue[_value - 1], _position.x, _position.y);
    }
    else {
      sb.draw(AssetLoader._diceWhite[_value - 1], _position.x, _position.y);
    }
  }
  
  @Override
  public Animation getAnimation() {
    if(_spinning == false) {
      return null;
    }
    
    if(_c == Color.BLUE) {
      return AssetLoader._diceAnimationBlue;
    }
    else {
      return AssetLoader._diceAnimationWhite;
    }
  }
  
  public void setRoll(int value) {
    _value = value;
  }
  
  public void setSpinning(boolean b) {
    _spinning = b;
  }
  
  public SpinningDice.Color getColor() {
    return _c;
  }
}
