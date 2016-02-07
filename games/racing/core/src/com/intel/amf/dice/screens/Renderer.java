package com.intel.amf.dice.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

public abstract class Renderer {
  protected OrthographicCamera _camera;

  public abstract void drawText(String s, float x, float y);
  public abstract void drawText(String s, float x, float y, Color c, float wrapWidth);
  public abstract float getFontHeight();
  public abstract float getFontWidth();

  public Camera getCamera() {
    return _camera;
  }
}