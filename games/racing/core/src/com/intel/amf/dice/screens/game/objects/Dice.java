package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.game.GameWorld;

public class Dice extends RenderObject implements Constants {
  protected int _value;
  public int _targetx;
  public int _targety;

  protected int _bezierx;
  protected int _beziery;

  protected float _duration;
  protected float _elapsed;

  protected int _startx;
  protected int _starty;
  
  protected int _corner;
  
  protected int [] _bezierStartx;
  protected int [] _bezierStarty;
  
  protected int [] _cornerx;
  protected int [] _cornery;
  
  protected SpinningDice _cornerDice;
  
  protected Car _car;

  public Dice(GameWorld w, int value, int corner) {
    super(w);
    _duration = 1.0f;
    _elapsed = 0.0f;
    _value = value;
    _corner = corner;
    _bezierStartx = new int[]{400, 400, 800, 400};
    _bezierStarty = new int[]{400, 400, 800, 800};
    _cornerx = new int[]{0 * TILE_SIZE + CORNER_DICE_OFFSET, 9 * TILE_SIZE + CORNER_DICE_OFFSET, 0 * TILE_SIZE + CORNER_DICE_OFFSET, 9 * TILE_SIZE + CORNER_DICE_OFFSET};
    _cornery = new int[]{0 * TILE_SIZE + CORNER_DICE_OFFSET, 0 * TILE_SIZE + CORNER_DICE_OFFSET, 4 * TILE_SIZE + CORNER_DICE_OFFSET, 4 * TILE_SIZE + CORNER_DICE_OFFSET};
    _position.x = _cornerx[_corner];
    _position.y = _cornery[_corner];
    
    SpinningDice sd = w.getCornerDice()[corner];
    sd.setRoll(value);
    sd.setSpinning(false);
    _cornerDice = sd;
  }

  @Override
  protected void updateObject(float delta) {
    _elapsed += delta;
    float t = _elapsed / _duration;
    _position.x = (1 - t) * (1 - t) * _startx + 2 * (1 - t) * t * _bezierx + t * t * _targetx;
    _position.y = (1 - t) * (1 - t) * _starty + 2 * (1 - t) * t * _beziery + t * t * _targety;
    
    if(Math.abs(_position.x - _targetx) < 20 && Math.abs(_position.y - _targety) < 20) {
      _live = false;
      _car.go(true);
      _cornerDice.setSpinning(true);
    }
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {
    if(_cornerDice.getColor() == SpinningDice.Color.WHITE) {
      sb.draw(AssetLoader._diceWhite[_value - 1], _position.x - 18, _position.y - 18);
    }
    else {
      sb.draw(AssetLoader._diceBlue[_value - 1], _position.x - 18, _position.y - 18);
    }
  }

  public void setTargetPosition(Car c) {
    float x = c.getPosition().x;
    float y = c.getPosition().y;
    
    _startx = (int)_position.x;
    _starty = (int)_position.y;

    _targetx = (int)x;
    _targety = (int)y;
    
    _bezierx = _bezierStartx[_corner];
    _beziery = _bezierStarty[_corner];
    
    c.incrementPathIndex(_value * 3);
    _car = c;
  }
}
