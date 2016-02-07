package com.intel.amf.dice.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.input.GestureDetector;
import com.intel.amf.dice.AMFRacing;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.Gestures;

public class GameScreen implements Screen {
  protected GameWorld _world;
  protected GameRenderer _renderer;
  protected float _runTime;

  public GameScreen(AMFRacing amfr) {
    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    float gameWidth = Constants.GAME_WIDTH;
    float gameHeight = screenHeight / (screenWidth / gameWidth);
    int midPointY = (int) (gameHeight / 2);
    
    System.out.println(screenWidth + " x " + screenHeight);
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