package com.intel.amf.dice.screens.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.Orchestration;
import com.intel.amf.dice.Orchestration.WorkHandler;
import com.intel.amf.dice.Singleton;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.ShapeObject;
import com.intel.amf.dice.screens.World;
import com.intel.amf.dice.screens.game.objects.Banner;
import com.intel.amf.dice.screens.game.objects.Car;
import com.intel.amf.dice.screens.game.objects.Countdown;
import com.intel.amf.dice.screens.game.objects.Dice;
import com.intel.amf.dice.screens.game.objects.Overlay;
import com.intel.amf.dice.screens.game.objects.SpinningDice;
import com.intel.amf.dice.screens.game.objects.StartBanner;

/**
 * A controller for the racing game
 * 
 * @author jkmathes
 */
public class GameWorld extends World implements Constants {
  /**
   * The objects currently in the game world
   */
  protected List<RenderObject> _objects;
  /**
   * A queue of objects which were created in the span of the current frame
   */
  protected List<RenderObject> _createdObjects;
  /**
   * The detected game height
   */
  protected int _gameHeight;
  /**
   * Whether or not we need to initialize the game. This is done
   * in frame = 0
   */
  protected boolean _init;
  /**
   * The 4 dice (one in each corner) which are spinning within the game
   */
  protected SpinningDice [] _cornerDice; 
  /**
   * The actual game screen mapping between tiles and IDs
   */
  public int[][] _map = new int[5][10];
  
  /**
   * The X,Y path which represents one circuit around the track
   */
  protected ArrayList<Vector2> _path;
  /**
   * The cars in play
   */
  protected Car [] _cars;
  /**
   * The communication and orchestration between the game
   * server and the physical game driver
   */
  protected Orchestration _orch;
  /**
   * A set of callbacks for items of work coming from the game server
   */
  protected HashMap<String, WorkHandler>  _handlers;
  /**
   * A set of adjustments to make to cars on the track, allowing them
   * to not overlap and create unique "lanes"
   */
  protected HashMap<Integer, GridPoint2> _projectionOffsets;
  /**
   * Whether or not the current game has completed
   */
  protected boolean _gameOver;
  /**
   * The winning car, when the game is over
   */
  protected int _winner;
  /**
   * Whether or not we have begun the game over fade
   */
  protected boolean _gameOverFadeBegin;
  protected ShapeObject _overlay;
  protected boolean _countdown;
  protected boolean _beginGame;
  protected StartBanner _startBanner;
  
  public GameWorld(float gameHeight) {
    super();
    createHandlers();
    _orch = new Orchestration(Singleton.getInstance().getGameHost());
    _objects = new ArrayList<RenderObject>();
    _createdObjects = new ArrayList<RenderObject>();
    _gameHeight = (int)gameHeight;
    _init = true;
    _countdown = true;
    
    /**
     * Create a mapping between the angle of the car and the 
     * amount to offset its projection. This is used to create
     * distinct lanes on the track
     */
    _projectionOffsets = new HashMap<Integer, GridPoint2>(); 
    _projectionOffsets.put(0, new GridPoint2(0, -PROJECTION_SIZE));
    _projectionOffsets.put(45, new GridPoint2(PROJECTION_SIZE, -PROJECTION_SIZE));
    _projectionOffsets.put(90, new GridPoint2(PROJECTION_SIZE, 0));
    _projectionOffsets.put(135, new GridPoint2(PROJECTION_SIZE, PROJECTION_SIZE));
    _projectionOffsets.put(180, new GridPoint2(0, PROJECTION_SIZE));
    _projectionOffsets.put(225, new GridPoint2(-PROJECTION_SIZE, PROJECTION_SIZE));
    _projectionOffsets.put(270, new GridPoint2(-PROJECTION_SIZE, 0));
    _projectionOffsets.put(-45, new GridPoint2(-PROJECTION_SIZE, -PROJECTION_SIZE));
    
    /**
     * Create the game map. Each integer maps to a
     * specific sprite or tile
     */
    _map[0] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    _map[1] = new int[]{0, 4, 3, 3, 1, 3, 3, 3, 5, 0};
    _map[2] = new int[]{0, 2, 0, 4, 3, 3, 5, 0, 2, 0};
    _map[3] = new int[]{0, 6, 3, 7, 0, 0, 6, 3, 7, 0};
    _map[4] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  }
  
  /**
   * Get the discovered game height. This is
   * calculated at init time
   * 
   * @return the physical game height
   */
  public int getGameHeight() {
    return _gameHeight;
  }

  /**
   * Get the objects currently in the game world
   * @return the set of in-play objects
   */
  public List<RenderObject> getObjects() {
    return _objects;
  }

  /**
   * Add an object to the game world. This first gets
   * added to the creation queue, and then added into
   * play in the following frame
   * 
   * @param ro the object to add into the game
   */
  public void addObject(RenderObject ro) {
    _createdObjects.add(ro);
  }

  /**
   * Get the projection adjustment for a specific
   * rotational value of the car.
   * 
   * @param r the current rotation of the car
   * @return the amount to adjust the position of the car
   */
  public GridPoint2 getProjectionOffset(int r) {
    return _projectionOffsets.get(r);
  }
  
  /**
   * Indicate that a car has finished the track path
   * @param carIndex the car which has completed the track
   */
  public void setFinished(int carIndex) {
    if(_gameOver == false) {
      _gameOver = true;
      _winner = carIndex;
      _orch.sendWin(_winner);
    }
  }
  
  /**
   * Update the entire game world
   * 
   * @param delta the time difference between the last frame and the current frame
   */
  public void update(float delta) {
    /**
     * Normalize the frame rate
     */
    if(delta >= 0.15f) {
      delta = 0.15f;
    }

    if(_gameOver) {
      if(_gameOverFadeBegin) {
      }
      else {
        _gameOverFadeBegin = true;
        _overlay = new Overlay(this, 0, 0, GAME_WIDTH, _gameHeight);
        addObject(_overlay);
        _cars[_winner].go(false);
        _cars[_winner].win();
        addObject(new Banner(this, _winner));
        _objects.remove(_cars[_winner]);
        _objects.add(_cars[_winner]);
        Timer.schedule(new Task() {

          @Override
          public void run() {
            Singleton.getInstance().getGame().setScreen(new GameScreen(Singleton.getInstance().getGame()));
          }
        }, 7f);
      }
    }
    
    /**
     * If there is work from the server, get it, translate to JSON, and
     * process the work
     */
    if(_orch.hasWork()) {
      String msg = _orch.getWork();
      if(msg == null || msg.length() == 0) {
        System.err.println("Unable to connect to dice bridge");
      }
      else {
        try {
          JsonValue j = new JsonReader().parse(msg);
          parseMessage(j);
        }
        catch(SerializationException se) {
          se.printStackTrace();
        }
      }
    }
    
    /**
     * If we still have yet to populate the game, do it now
     */
    if(_init) {
      init();
    }
      
    if(_countdown) {
      _startBanner = new StartBanner(this, 0, 0, GAME_WIDTH, _gameHeight);
      addObject(_startBanner);
      _countdown = false;
      Countdown cd = new Countdown(this, 3);
      addObject(cd);
    }

    /*
    if(_beginGame) {
      for(int f = 0; f < _cars.length; f++) {
        roll(f, MathUtils.random(1, 6));
      }
    }
    */

    /**
     * The "flung" queue is a set of objects
     * which have been dragged or thrown.
     * This is used only on touch screens.
     */
    processFlungQueue();
    
    /**
     * Update all the objects currently in play
     */
    processObjects(delta);
    
    /**
     * For all new objects, add them into play for
     * the subsequent frame
     */
    processCreatedObjects();
  }

  public void beginGame() {
    _beginGame = true;
  }
  
  public StartBanner getStartBanner() {
    return _startBanner;
  }
  
  /**
   * Initialize the objects at the start of 
   * the game world
   */
  protected void init() {
    _init = false;
    _cornerDice = new SpinningDice[4];
    SpinningDice d = new SpinningDice(this, SpinningDice.Color.BLUE);
    d.setX(CORNER_DICE_OFFSET);
    d.setY(CORNER_DICE_OFFSET);
    addObject(d);
    _cornerDice[0] = d;
    
    d = new SpinningDice(this, SpinningDice.Color.WHITE);
    d.setX(9 * TILE_SIZE + CORNER_DICE_OFFSET);
    d.setY(CORNER_DICE_OFFSET);
    addObject(d);
    _cornerDice[1] = d;
          
    d = new SpinningDice(this, SpinningDice.Color.WHITE);
    d.setX(CORNER_DICE_OFFSET);
    d.setY(4 * TILE_SIZE + CORNER_DICE_OFFSET);
    addObject(d);
    _cornerDice[2] = d;
    
    d = new SpinningDice(this, SpinningDice.Color.BLUE);
    d.setX(9 * TILE_SIZE + CORNER_DICE_OFFSET);
    d.setY(4 * TILE_SIZE + CORNER_DICE_OFFSET);
    addObject(d);
    _cornerDice[3] = d;
    
    _path = CarPath.createPath();
    _cars = new Car[4];
    for(int f = 0; f < _cars.length; f++) {
      _cars[f] = new Car(this, (int)_path.get(0).x, (int)_path.get(0).y, 42, 28, f);
      addObject(_cars[f]);
    }
  }
  
  /**
   * Initialize the callbacks for commands coming from 
   * the game server
   */
  protected void createHandlers() {
    _handlers = new HashMap<String, WorkHandler>();
    _handlers.put("roll", new WorkHandler() {

      @Override
      public void handle(JsonValue j) {
        int carIndex = j.getInt("dice");
        int value = j.getInt("value");
        roll(carIndex, value);
      }
    });
  }
  
  /**
   * Parse a command from the game server
   * 
   * @param j the JSON command to parse
   */
  public void parseMessage(JsonValue j) {
    String type = j.getString("type");
    JsonValue payload = j.get("payload");
    if(type != null && payload != null) {
      _handlers.get(type).handle(payload);
    }
  }
  
  /**
   * Initiate a dice roll within the game. This will
   * cause a die to fling from one of the corners
   * 
   * @param corner the dice ID (corner ID) to throw
   * @param value the value of the die
   */
  public void roll(int corner, int value) {
    if(corner < 0 || corner >= _cars.length || value < 1 || value > 6) {
      return;
    }
    if(_cars[corner].inMotion() == false && _beginGame) {
      Dice d = new Dice(this, value, corner);
      d.setTargetPosition(_cars[corner]);
      addObject(d);
    }
  }
  
  /**
   * Get the set of dice which are spinning in the 4 corners
   * @return the corner dice set
   */
  public SpinningDice [] getCornerDice() {
    return _cornerDice;
  }
  
  public void setRenderer(GameRenderer r) {
  }

  /**
   * Get the track path that cars should follow
   * @return the list of coordinates to use for a path along the track
   */
  public ArrayList<Vector2> getPath() {
    return _path;
  }
  
  /**
   * Take the queue of objects created in the current frame and add
   * them into the game world
   */
  public void processCreatedObjects() {
    for(Iterator<RenderObject> i = _createdObjects.iterator(); i.hasNext(); ) {
      RenderObject ro = i.next();
      i.remove();
      _objects.add(ro);
    }
  }

  /**
   * Update all in-play game objects, and remove the ones which are 
   * no longer in play
   * 
   * @param delta the time delta between the previous frame and this frame
   */
  public void processObjects(float delta) {
    for(Iterator<RenderObject> i = _objects.iterator(); i.hasNext(); ) {
      RenderObject o = i.next();
      o.update(delta);

      if(o.isLive() == false) {
        i.remove();
      }
    }
  }

  /**
   * When a touch screen is available, process all objects which have been
   * "grabbed" by the finger. The fling velocity is preserved in the game object
   */
  public void processFlungQueue() {
    for(Iterator<float []> iter = _flingQueue.iterator(); iter.hasNext(); ) {
      float [] f = iter.next();
      for(RenderObject s : _objects) {
        if(s.isFlingable()) {
          if(s.getX() - TOUCH_BUFFER < f[0] && (s.getX() + s.getWidth() + TOUCH_BUFFER) > f[0] &&
                  s.getY() - TOUCH_BUFFER < f[1] && (s.getY() + s.getHeight() + TOUCH_BUFFER) > f[1]) {

            _grabbed = s;

            _grabbed.setVelocity(new Vector2(0f, 0f));
            _grabbed.setAcceleration(new Vector2(0f, 0f));
            _grabbed.setGrabbed(true);
            break;
          }
        }
      }

      iter.remove();
    }
  }
}
