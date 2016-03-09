package com.intel.amf.dice.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.intel.amf.dice.AssetLoader;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.Renderer;

public class GameRenderer extends Renderer implements Constants {
  protected float _scaleX;
  protected float _scaleY;
  protected GameWorld _world;
  protected SpriteBatch _batcher;
  protected ShapeRenderer _shapeRenderer;
  protected int _gameHeight;
  protected BitmapFont _font;
  protected float _fontWidth;
  protected float _fontHeight;
  protected Color _fontColor;
  protected float _goalCounter;
  protected int _goalFrame;

  public GameRenderer(GameWorld world, int gameHeight, int midPointY) {
    _world = world;
    _camera = new OrthographicCamera();
    _camera.setToOrtho(true, Constants.GAME_WIDTH, gameHeight);
    _batcher = new SpriteBatch();
    _batcher.setProjectionMatrix(_camera.combined);
    _shapeRenderer = new ShapeRenderer();
    _shapeRenderer.setProjectionMatrix(_camera.combined);
    _gameHeight = gameHeight;
    _font = AssetLoader._font;
    //_font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    _fontHeight = FONT_HEIGHT * FONT_SCALE;
    _fontWidth = FONT_WIDTH * FONT_SCALE;
    _fontColor = new Color(0x747474);
    _goalCounter = 0f;
    _goalFrame = 0;
  }

  public void render(float delta, float runTime) {
    _batcher.enableBlending();

    _batcher.begin();
    for(RenderObject ro : _world.getObjects()) {
      if(ro.needsTextCalculation()) {
        ro.adjustText(_batcher);
      }
    }
    _batcher.end();

    drawBackground(delta);

    for(RenderObject ro : _world.getObjects()) {
      if(ro.isShape()) {
        ro.renderShape(_shapeRenderer);
      }
    }

    _batcher.begin();

    for(RenderObject ro : _world.getObjects()) {
      if(ro.getAnimation() != null) {
        _batcher.draw(ro.getAnimation().getKeyFrame(runTime, true), ro.getPosition().x, ro.getPosition().y);
      }
      else {
        if(ro.needsOwnBatcher()) {
          _batcher.end();
          _batcher.begin();
        }
        ro.render(_batcher, this);
        if(ro.needsOwnBatcher()) {
          _batcher.end();
          _batcher.begin();
        }
      }
    }

    _batcher.end();
  }

  @Override
  public void drawText(String s, float x, float y) {
    drawText(s, x, y, null, GAME_WIDTH);
  }

  @Override
  public void drawText(String s, float x, float y, Color c, float wrapWidth) {
    Color cc = _font.getColor();
    if(c != null) {
      _font.setColor(c);
    }
    _font.draw(_batcher, s, x, y);
    _font.setColor(cc);
  }

  protected void drawBackground(float delta) {
    Gdx.gl.glClearColor( 0, 0, 0, 1 );
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
    
    _batcher.begin();
    for(int f = 0; f < 10; f++) {
      for(int g = 0; g < 5; g++) {
        _batcher.draw(AssetLoader._sprites[0], f * 128, g * 128);
        if(_world._map[g][f] > 0) {
          _batcher.draw(AssetLoader._sprites[_world._map[g][f]], f * 128, g * 128);
        }
      }
    }
    _batcher.draw(AssetLoader._backrock, 0, 0);
    _batcher.draw(AssetLoader._backrock, 0, 4 * 128);
    _batcher.draw(AssetLoader._backrock, 9 * 128, 0);
    _batcher.draw(AssetLoader._backrock, 9 * 128, 4 * 128);
    _batcher.draw(AssetLoader._logo, 490, 412, 0, 0, 300, 200, 1.0f, 1.0f, 0f);
    _batcher.draw(AssetLoader._amf, 435, 22);
    //_batcher.draw(AssetLoader._logo, 4 * 128, 2 * 128 + 20, 0, 0, 104, 70, 1.0f, 1.0f, 0f);
    //AssetLoader._font.draw(_batcher, "Test321", 5 * 128, 5 * 128 - 20);
    //drawText("Test321", 5 * 128, 5 * 128 - 20, Color.YELLOW, 10);
    _batcher.end();
  }
  
  @Override
  public float getFontHeight() {
    return _fontHeight;
  }

  @Override
  public float getFontWidth() {
    return _fontWidth;
  }
}
