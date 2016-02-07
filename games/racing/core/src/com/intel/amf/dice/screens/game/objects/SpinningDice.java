package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.World;

public class SpinningDice extends RenderObject {
  private SpinningDice.Color _c;
  
  public static enum Color { BLUE, WHITE };
  
  public SpinningDice(World w, SpinningDice.Color c) {
    super(w);
    _c = c;
  }

  @Override
  protected void updateObject(float delta) {
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
  }
  
  @Override
  public Animation getAnimation() {
    if(_c == Color.BLUE) {
      return AssetLoader._diceAnimationBlue;
    }
    else {
      return AssetLoader._diceAnimationWhite;
    }
  }
}
