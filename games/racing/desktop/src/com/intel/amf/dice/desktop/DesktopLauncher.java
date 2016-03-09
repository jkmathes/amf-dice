package com.intel.amf.dice.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.intel.amf.dice.AMFRacing;
import com.intel.amf.dice.Singleton;

public class DesktopLauncher {
	public static void main (String[] arg) {
	  if(arg.length > 0 && arg[0] != null) {
	    Singleton.getInstance().setGameHost(arg[0]);
	  }
	  else {
	    Singleton.getInstance().setGameHost("localhost");
	  }
	  
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.fullscreen = false;
		new LwjglApplication(new AMFRacing(), config);
	}
}
