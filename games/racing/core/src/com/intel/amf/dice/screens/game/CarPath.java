package com.intel.amf.dice.screens.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

/**
 * A path generator for a car to travel against
 * 
 * This is based on a game of width = 1280
 * 
 * @author jkmathes
 */
public class CarPath {
  public static ArrayList<Vector2> createPath() {
    ArrayList<Vector2> path;
    path = new ArrayList<Vector2>();
    int x;
    int y = 192;    
    for(x = 544; x < 1040; x += 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; x < 1088; x += 16) {
      path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; y < 400; y += 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; x > 1040; x -= 16) {
      path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; x > 880; x -= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; y > 400; y -= 16) {
      path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; y > 368; y -= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; y > 320; y -= 16) {
      path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; x > 496; x -= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; x > 448; x -= 16) {
      path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; y < 400; y+= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; y < 448; y += 16) {
      path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; x > 240; x -= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; y > 400; y -= 16) {
      path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; y > 240; y -= 16) {
      path.add(new Vector2(x, y));
    }
    
    for(; x < 240; x += 16) {
      path.add(new Vector2(x, y));
      y -= 16;
    }
    
    for(; x < 560; x += 16) {
      path.add(new Vector2(x, y));
    }
    return path;
  }
}
