package com.intel.amf.dice;

import com.badlogic.gdx.Game;
import com.intel.amf.dice.screens.game.GameScreen;

public class AMFRacing extends Game {
  @Override
  public void create() {
    AssetLoader.load();
    setScreen(new GameScreen(this));
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }
}
