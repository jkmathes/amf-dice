package com.intel.amf.dice;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.intel.amf.dice.screens.game.GameScreen;

public class AMFRacing extends Game {
  @Override
  public void create() {
    AssetLoader.load();
    AssetLoader._music.setLooping(true);
    //AssetLoader._music.play();
    Gdx.net = new GameNetImpl();
    Singleton.getInstance().setGame(this);
    setScreen(new GameScreen(this));
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }
}
