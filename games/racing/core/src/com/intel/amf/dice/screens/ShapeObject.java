package com.intel.amf.dice.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * An object that is simply a colored shape
 * 
 * @author jkmathes
 */
public class ShapeObject extends RenderObject {
  protected Color _color;

  public ShapeObject(World w, int x, int y, int width, int height) {
    super(w);
    _position.x = x;
    _position.y = y;
    _width = width;
    _height = height;
  }

  @Override
  protected void updateObject(float delta) {
  }

  @Override
  public void render(SpriteBatch sb, Renderer r) {

  }

  @Override
  public boolean isShape() {
    return true;
  }

  protected boolean isTransparent() {
    return false;
  }

  @Override
  public void renderShape(ShapeRenderer r) {
    if(isTransparent()) {
      return;
    }
    r.begin(ShapeRenderer.ShapeType.Filled);
    r.setColor(_color);
    r.rect(_position.x, _position.y, _width, _height);
    r.end();
  }
}