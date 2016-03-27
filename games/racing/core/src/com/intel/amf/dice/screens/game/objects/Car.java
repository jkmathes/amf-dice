package com.intel.amf.dice.screens.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;
import com.intel.amf.dice.screens.game.GameWorld;

/**
 * A single car in the game world
 * 
 * @author jkmathes
 */
public class Car extends RenderObject implements Constants {
  /**
   * The current rotation of the car. This is used to make it
   * appear to turn around the track
   */
  protected float _rot;
  /**
   * The index along the travel path for this car
   */
  protected int _pathIndex;
  /**
   * The X position in the previous frame. This determines
   * which way the car should be pointing
   */
  protected int _lastX;
  /**
   * The Y position in the previous frame. This determines
   * which way the car should be pointing
   */
  protected int _lastY;
  /**
   * A duration counter used to smooth out the car updates
   */
  protected float _counter;
  /**
   * Where this car *should* be, based on rolls
   */
  protected int _targetPathIndex;
  /**
   * Whether or not this car should be in motion towards _targetPathIndex
   */
  protected boolean _go;
  /**
   * The car identifier, 0 through 3
   */
  protected int _carIndex;
  /**
   * The game world governing this car
   */
  protected GameWorld _w;
  /**
   * Whether or not this car has finished the race
   */
  protected boolean _finished;
  /**
   * Whether or not this car has won the race
   */
  protected boolean _win;
  /**
   * The current scale of the car - this is used to
   * zoom in on the winning car at the end of a race
   */
  protected float _scale;
  
  /**
   * Create a car in the game world
   * 
   * @param w the world governing this car
   * @param x the initial x position of the car
   * @param y the initial y position of the car
   * @param width the initial width of the car
   * @param height the initial height of the car
   * @param carIndex which car this is, 0 through 3
   */
  public Car(GameWorld w, int x, int y, int width, int height, int carIndex) {
    super(w);
    _w = w;
    _position.x = x;
    _position.y = y;
    _width = width;
    _height = height;
    _pathIndex = 0;
    _counter = 0;
    _lastX = x - 16;
    _lastY = y;
    _go = false;
    _win = false;
    _finished = false;
    _carIndex = carIndex;
    _scale = 1.0f;
  }

  @Override
  protected void updateObject(float delta) {    
    _counter += delta;
    if(_counter < 0.015) {
      return;
    }
    
    _counter = 0;
    
    /**
     * If this car has won, gradually move it to the center of the screen
     * while scaling it to 3x normal size
     */
    if(_win) {
      if((int)_position.x < (int)((GAME_WIDTH / 2) - (_width / 2))) {
        _position.x++;
      }
      else if((int)_position.x > (int)((GAME_WIDTH / 2) - (_width / 2))) {
        _position.x--;
      }
      if((int)_position.y < (int)((_w.getGameHeight() / 2) - (_height / 2))) {
        _position.y++;
      }
      else if((int)_position.y < (int)((_w.getGameHeight() / 2) - (_height / 2))) {
        _position.y--;
      }
      
      if(_scale <= 3.0f) {
        _scale += 0.1f;
      }
      else {
        
      }
      
      return;
    }
    
    /**
     * If this car should be in motion (meaning a dice collided with it),
     * and it has an index it should advance towards, increment the current
     * path index by 1 per frame
     */
    if(_go && _pathIndex < _targetPathIndex) {
      _pathIndex++;
    }
    
    /**
     * Determine if this car has finished the race
     */
    if(_pathIndex > ((GameWorld)_world).getPath().size() - 1) {
      _pathIndex = 0;
      _finished = true;
      _w.setFinished(_carIndex);
    }
    _position = ((GameWorld)_world).getPath().get(_pathIndex).cpy(); 
    
    int deltaX = (int)_position.x - _lastX;
    int deltaY = (int)_position.y - _lastY;
    
    /**
     * If X or Y changed, we can determine the rotation of the car
     * to simulate turning around corners on the track
     */
    if(deltaX > 0) {
      _rot = 0;
      if(deltaY < 0) {
        _rot -= 45;
      }
      else if(deltaY > 0) {
        _rot += 45;
      }
    }
    else if(deltaX < 0) {
      _rot = 180;
      if(deltaY < 0) {
        _rot += 45;
      }
      else if(deltaY > 0) {
        _rot -= 45;
      }
    }
    else {
      if(deltaY < 0) {
        _rot = 270;
      }
      else if(deltaY > 0) {
        _rot = 90;
      }
    } 
    
    _lastX = (int)_position.x;
    _lastY = (int)_position.y;
    
    /**
     * This adjusts the car along its particular piece of the track.
     * Each car has a projection which follows the path, but makes sure
     * it doesn't overlap with another car
     */
    GridPoint2 mod = ((GameWorld)_world).getProjectionOffset((int)_rot);
    
    int modx = mod.x * _carIndex + mod.x;
    int mody = mod.y * _carIndex + mod.y;
    
    if(mod.x > 0) {
      modx -= (PROJECTION_SIZE * 2 + (PROJECTION_SIZE / 2));
    }
    else if(mod.x < 0) {
      modx += (PROJECTION_SIZE * 2 + (PROJECTION_SIZE / 2));
    }
    if(mod.y > 0) {
      mody -= (PROJECTION_SIZE * 2 + (PROJECTION_SIZE / 2));
    }
    else if(mod.y < 0) {
      mody += (PROJECTION_SIZE * 2 + (PROJECTION_SIZE / 2));
    }
    
    _position.x += modx;
    _position.y += mody;
  }
  
  /**
   * Increment this car along the track by a given
   * offset. Each offset corresponds to an index
   * in the CarPath
   * 
   * @param inc the amount by which to increment
   */
  public void incrementPathIndex(int inc) {
    _targetPathIndex = _pathIndex + inc;
  }
  
  /**
   * Whether or not this car is in motion.
   * 
   * @return true if this car has not yet reached its destination, false otherwise
   */
  public boolean inMotion() {
    if(_pathIndex < _targetPathIndex) {
      return true;
    }
    _go = false;
    return false;
  }
  
  /**
   * Declare this car a winner. This triggers the zooming action
   */
  public void win() {
    _win = true;
  }
  
  /**
   * Declare this car able to start moving
   * 
   * @param b true if this car should advance to its next position, false otherwise
   */
  public void go(boolean b) {
    _go = b;
  }
  
  @Override
  public void render(SpriteBatch sb, Renderer r) {
    sb.draw(AssetLoader._cars[_carIndex], getX() - (getWidth() / 2), getY() - (getHeight() / 2), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), _scale, _scale, _rot);
  }
}
