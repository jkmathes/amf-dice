package com.intel.amf.dice;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AssetLoader {
  public static TextureRegion [] _sprites;
  public static Animation _diceAnimationBlue;
  public static Animation _diceAnimationWhite;
  public static TextureRegion [] _diceWhite;
  public static TextureRegion [] _diceBlue;
  public static TextureRegion _backrock;
  public static TextureRegion [] _cars; 
  public static TextureRegion _logo;
  
  public static void load() {
    _sprites = new TextureRegion[Constants.SPRITES_NEEDED.length];
    String root = "sprites/images/Car-Racing-Game-Tileset_";
    for(int f = 0; f < Constants.SPRITES_NEEDED.length; f++) {
      Texture t = new Texture(Gdx.files.internal(root + Constants.SPRITES_NEEDED[f] + ".png"));
      t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
      TextureRegion tr = new TextureRegion(t, 0, 0, 128, 128);
      tr.flip(false, true);
      _sprites[f] = tr;
    }
    
    TextureRegion [] frames = new TextureRegion[114];
    Texture t = new Texture(Gdx.files.internal("dice_rolls_blue.png"));
    t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    frames[0] = new TextureRegion(t, 0, 0, 37, 37);
    int counter = 1;
    for(int g = 1; g < 8; g++) {
      for(int f = 0; f < 16; f++) {
        frames[counter] = new TextureRegion(t, f * 37, g * 37, 37, 37);
        counter++;
      }
    }
    frames[counter] = new TextureRegion(t, 0, 8 * 37, 37, 37);
    _diceAnimationBlue = new Animation(0.005f, frames);    
    
    frames = new TextureRegion[114];
    t = new Texture(Gdx.files.internal("dice_rolls_white.png"));
    t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    frames[0] = new TextureRegion(t, 0, 0, 37, 37);
    counter = 1;
    for(int g = 1; g < 8; g++) {
      for(int f = 0; f < 16; f++) {
        frames[counter] = new TextureRegion(t, f * 37, g * 37, 37, 37);
        counter++;
      }
    }
    frames[counter] = new TextureRegion(t, 0, 8 * 37, 37, 37);
    _diceAnimationWhite = new Animation(0.005f, frames);    
    
    t = new Texture(Gdx.files.internal("backrock.png"));
    t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    TextureRegion tr = new TextureRegion(t, 0, 0, 128, 128);
    tr.flip(false, true);
    _backrock = tr;
    
    // 517 (5), 70 (42x28)
    _cars = new TextureRegion[4];
    String [] carIndexes = new String[]{"05.png", "07.png", "15.png", "17.png"};
    for(int f = 0; f < carIndexes.length; f++) {
      t = new Texture(Gdx.files.internal(root + carIndexes[f]));
      t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
      tr = new TextureRegion(t, 5, 48, 42, 28);
      tr.flip(false, true);
      _cars[f] = tr;
    }
    
    t = new Texture(Gdx.files.internal("intel-logo.png"));
    t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    tr = new TextureRegion(t, 0, 0, 2000, 1324);
    tr.flip(false, true);
    _logo = tr;
    
    _diceWhite = new TextureRegion[6];
    for(int f = 0; f < 6; f++) {
      t = new Texture(Gdx.files.internal("dice_white.png"));
      t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
      tr = new TextureRegion(t, f * 37, 0, 37, 37);
      tr.flip(false, true);
      _diceWhite[f] = tr;
    }
    
    _diceBlue = new TextureRegion[6];
    for(int f = 0; f < 6; f++) {
      t = new Texture(Gdx.files.internal("dice_blue.png"));
      t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
      tr = new TextureRegion(t, f * 37, 0, 37, 37);
      tr.flip(false, true);
      _diceBlue[f] = tr;
    }
  }
  
  public static void dispose() {
    
  }
}
