package com.intel.amf.dice.screens;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * An object in the game world. Each object has its own position, accleration, and velocity
 * 
 * @author jkmathes
 */
public abstract class RenderObject {
  protected Vector2 _position;
  protected Vector2 _velocity;
  protected Vector2 _acceleration;
  protected float _rotation;
  protected boolean _live;
  protected int _height;
  protected int _width;
  protected Animation _animation;
  protected boolean _flung;
  protected boolean _grabbed;
  protected boolean _flingable;
  protected boolean _shape;
  protected World _world;

  public RenderObject(World w) {
    _live = true;
    _world = w;
    _velocity = new Vector2();
    _acceleration = new Vector2();
    _position = new Vector2();
  }

  protected abstract void updateObject(float delta);

  public void update(float delta) {
    setVelocity(_velocity.add(_acceleration.cpy().scl(delta)));
    setPosition(_position.add(_velocity.cpy().scl(delta)));
    updateObject(delta);
  }

  public boolean needsTextCalculation() {
    return false;
  }

  public void adjustText(SpriteBatch sb) {
  }

  public boolean isFlung() {
    return _flung;
  }

  public void setFlung(boolean b) {
    _flung = b;
  }

  public boolean isGrabbed() {
    return _grabbed;
  }

  public void setGrabbed(boolean b) {
    _grabbed = b;
  }

  public int getHeight() {
    return _height;
  }

  public int getWidth() {
    return _width;
  }

  public void setPosition(Vector2 p) {
    _position = p;
  }

  public void setX(float x) {
    _position.x = x;
  }

  public void setY(float y) {
    _position.y = y;
  }

  public Vector2 getPosition() {
    return _position;
  }

  public float getX() {
    return _position.x;
  }

  public float getY() {
    return _position.y;
  }

  public Vector2 getVelocity() {
    return _velocity;
  }

  public void setVelocity(Vector2 v) {
    _velocity = v;
  }

  public Vector2 getAcceleration() {
    return _acceleration;
  }

  public void setAcceleration(Vector2 v) {
    _acceleration = v;
  }

  public float getRotation() {
    return _rotation;
  }

  public void setRotation(float r) {
    _rotation = r;
  }

  public boolean isLive() {
    return _live;
  }

  public void setLive(boolean b) {
    _live = b;
  }

  public Animation getAnimation() {
    return _animation;
  }

  public boolean isFlingable() {
    return _flingable;
  }

  public boolean isShape() {
    return false;
  }

  public abstract void render(SpriteBatch sb, Renderer r);

  public void renderShape(ShapeRenderer r) {
  }

  public boolean shouldPrepend() {
    return false;
  }

  public boolean needsOwnBatcher() {
    return false;
  }

}