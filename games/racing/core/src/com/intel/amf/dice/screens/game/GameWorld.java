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
import com.intel.amf.dice.Constants;
import com.intel.amf.dice.Orchestration;
import com.intel.amf.dice.Orchestration.WorkHandler;
import com.intel.amf.dice.screens.RenderObject;
import com.intel.amf.dice.screens.World;
import com.intel.amf.dice.screens.game.objects.Car;
import com.intel.amf.dice.screens.game.objects.Dice;
import com.intel.amf.dice.screens.game.objects.SpinningDice;

public class GameWorld extends World implements Constants {
  protected List<RenderObject> _objects;
  protected List<RenderObject> _createdObjects;
  protected int _gameHeight;
  protected boolean _init; 
  protected SpinningDice [] _cornerDice; 
  public int[][] _map = new int[5][10];
  
  protected ArrayList<Vector2> _path;
  protected Car [] _cars;
  protected boolean _launched;
  
  protected Orchestration _orch;
  protected HashMap<String, WorkHandler>  _handlers;
  
  protected HashMap<Integer, GridPoint2> _projectionOffsets;
  
  public GameWorld(float gameHeight) {
    super();
    createHandlers();
    _orch = new Orchestration("http://localhost:8000/work");
    _objects = new ArrayList<RenderObject>();
    _createdObjects = new ArrayList<RenderObject>();
    _gameHeight = (int)gameHeight;
    _init = true;
    
    _projectionOffsets = new HashMap<Integer, GridPoint2>(); 
    _projectionOffsets.put(0, new GridPoint2(0, -PROJECTION_SIZE));
    _projectionOffsets.put(45, new GridPoint2(PROJECTION_SIZE, -PROJECTION_SIZE));
    _projectionOffsets.put(90, new GridPoint2(PROJECTION_SIZE, 0));
    _projectionOffsets.put(135, new GridPoint2(PROJECTION_SIZE, PROJECTION_SIZE));
    _projectionOffsets.put(180, new GridPoint2(0, PROJECTION_SIZE));
    _projectionOffsets.put(225, new GridPoint2(-PROJECTION_SIZE, PROJECTION_SIZE));
    _projectionOffsets.put(270, new GridPoint2(-PROJECTION_SIZE, 0));
    _projectionOffsets.put(-45, new GridPoint2(-PROJECTION_SIZE, -PROJECTION_SIZE));
    
    _map[0] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    _map[1] = new int[]{0, 4, 3, 3, 1, 3, 3, 3, 5, 0};
    _map[2] = new int[]{0, 2, 0, 4, 3, 3, 5, 0, 2, 0};
    _map[3] = new int[]{0, 6, 3, 7, 0, 0, 6, 3, 7, 0};
    _map[4] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  }
  
  public int getGameHeight() {
    return _gameHeight;
  }

  public List<RenderObject> getObjects() {
    return _objects;
  }

  public void addObject(RenderObject ro) {
    _createdObjects.add(ro);
  }

  public GridPoint2 getProjectionOffset(int r) {
    return _projectionOffsets.get(r);
  }
  
  public void update(float delta) {
    if(delta >= 0.15f) {
      delta = 0.15f;
    }

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
    
    if(_init) {
      init();
    }
        
    processFlungQueue();
    processObjects(delta);
    processCreatedObjects();
  }

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
  
  public void parseMessage(JsonValue j) {
    String type = j.getString("type");
    JsonValue payload = j.get("payload");
    if(type != null && payload != null) {
      _handlers.get(type).handle(payload);
    }
  }
  
  public void roll(int corner, int value) {
    if(_cars[corner].inMotion() == false) {
      Dice d = new Dice(this, value, corner);
      d.setTargetPosition(_cars[corner]);
      addObject(d);
    }
  }
  
  public SpinningDice [] getCornerDice() {
    return _cornerDice;
  }
  
  public void setRenderer(GameRenderer r) {
  }

  public ArrayList<Vector2> getPath() {
    return _path;
  }
  
  public void processCreatedObjects() {
    for(Iterator<RenderObject> i = _createdObjects.iterator(); i.hasNext(); ) {
      RenderObject ro = i.next();
      i.remove();
      _objects.add(ro);
    }
  }

  public void processObjects(float delta) {
    for(Iterator<RenderObject> i = _objects.iterator(); i.hasNext(); ) {
      RenderObject o = i.next();
      o.update(delta);

      if(o.isLive() == false) {
        i.remove();
      }
    }
  }

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
