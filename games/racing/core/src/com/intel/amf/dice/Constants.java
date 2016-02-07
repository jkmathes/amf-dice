package com.intel.amf.dice;

public interface Constants {
  public static final int GAME_WIDTH = 1280;
  public static final int GAME_HEIGHT = 720;

  public static final float TOUCH_BUFFER = 15f;

  public static float FONT_HEIGHT = 101;
  public static float FONT_WIDTH = 51;
  public static float FONT_SCALE = 0.5f;

  //                                                    Grass Start Vert  Horiz UL    UR    LL    LR
  //                                                    0     1     2     3     4     5     6     7
  public static String [] SPRITES_NEEDED = new String[]{"03", "22", "31", "32", "41", "42", "51", "52"};
}