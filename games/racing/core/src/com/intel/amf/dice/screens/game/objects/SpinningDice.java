package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.World;

/**
 * A rotating dice in the game world
 * 
 * @author jkmathes
 */
public class SpinningDice extends RenderObject {
  /**
   * The color of the spinning dice. Can be blue or white
   */
  private SpinningDice.Color _c;
  /**
   * The value of the dice at rest
   */
  private int _value;
  /**
   * Whether or not the dice is rotating (meaning has not been rolled and landed)
   */
  private boolean _spinning;
  /**
   * The colors a dice can take
   */
  public static enum Color { BLUE, WHITE };
  
  /**
   * Create a spinning dice in the game world
   * @param w the world governing this dice
   * @param c the dice color, BLUE or WHITE
   */
  public SpinningDice(World w, SpinningDice.Color c) {
    super(w);
    _spinning = true;
    _c = c;
  }

  @Override
  protected void updateObject(float delta) {
  }

  /**
   * Render a spinning dice
   * 
   * If the dice is spinning, the renderer will pick this up
   * during the frame animation phase. Otherwise, render
   * the static value of the dice at rest
   * 
   * @param sb the sprite batcher to render against
   * @param r the game renderer to use
   */
  @Override
  public void render(SpriteBatch sb, Renderer r) {
    if(_spinning) {
      return;
    }
    
    TextureRegion trd = AssetLoader._diceWhite[_value - 1];
    if(_c == Color.BLUE) {
      trd = AssetLoader._diceBlue[_value - 1];
    }
    sb.draw(trd, _position.x, _position.y);
  }
  
  /**
   * Render an animated spinning dice
   * 
   * If the dice is at rest, the batcher will pick this up
   * during the static rendering phase. Otherwise, return the
   * animation to the renderer
   * 
   * @return the animation to use for this spinning dice
   */
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
