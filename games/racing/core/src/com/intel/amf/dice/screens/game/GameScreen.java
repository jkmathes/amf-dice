package com.intel.amf.dice.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.input.GestureDetector;
import com.intel.amf.dice.AMFRacing;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.Gestures;

/**
 * This represents the game screen
 *  
 * @author jkmathes
 */
public class GameScreen implements Screen {
  /**
   * The controller for this game
   */
  protected GameWorld _world;
  /**
   * The view renderer to use
   */
  protected GameRenderer _renderer;
  /**
   * Elapsed time in game
   */
  protected float _runTime;

  /**
   * Create a game screen on the current device
   * @param amfr the game driver requesting this screen
   */
  public GameScreen(AMFRacing amfr) {
    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    float gameWidth = Constants.GAME_WIDTH;
    float gameHeight = screenHeight / (screenWidth / gameWidth);
    int midPointY = (int) (gameHeight / 2);
    
    _world = new GameWorld(gameHeight);

    final Gestures gestures = new Gestures(_world, screenWidth / gameWidth, screenHeight / gameHeight);
    Gdx.input.setInputProcessor(new GestureDetector(gestures) {
      @Override
      public boolean keyDown(int k) {
        if(k == Input.Keys.BACK) {
          return true;
        }
        else if(k == Input.Keys.ENTER || k == Input.Keys.ESCAPE) {
          Gdx.app.exit();
        }
        return super.keyDown(k);
      }
      
    });
    Gdx.input.setCatchBackKey(true);

    _renderer = new GameRenderer(_world, (int) gameHeight, midPointY);
    _world.setRenderer(_renderer);
  }

  @Override
  public void show() {

  }

  /**
   * Render a single frame in the game. This will
   * update all objects in the world, and render the frame
   */
  @Override
  public void render(float delta) {
    _runTime += delta;
    _world.update(delta);
    _renderer.render(delta, _runTime);
  }

  @Override
  public void resize(int width, int height) {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void hide() {

  }

  @Override
  public void dispose() {

  }
}