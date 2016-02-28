package com.intel.amf.dice.screens.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.intel.amf.dice.Constants;
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

  public GameWorld(float gameHeight) {
    super();
    _objects = new ArrayList<RenderObject>();
    _createdObjects = new ArrayList<RenderObject>();
    _gameHeight = (int)gameHeight;
    _init = true;
    
    _map[0] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    _map[1] = new int[]{0, 4, 3, 3, 1, 3, 3, 3, 5, 0};
    _map[2] = new int[]{0, 2, 0, 4, 3, 3, 5, 0, 2, 0};
    _map[3] = new int[]{0, 6, 3, 7, 0, 0, 6, 3, 7, 0};
    _map[4] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    
    _path = new ArrayList<Vector2>();
    int x;
    int y = 192;
    for(x = 544; x < 1040; x += 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; x < 1088; x += 16) {
      _path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; y < 400; y += 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; x > 1040; x -= 16) {
      _path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; x > 880; x -= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; y > 400; y -= 16) {
      _path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; y > 368; y -= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; y > 320; y -= 16) {
      _path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; x > 496; x -= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; x > 448; x -= 16) {
      _path.add(new Vector2(x, y));
      y += 16;
    }
    
    for(; y < 400; y+= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; y < 448; y += 16) {
      _path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; x > 240; x -= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; y > 400; y -= 16) {
      _path.add(new Vector2(x, y));
      x -= 16;
    }
    
    for(; y > 240; y -= 16) {
      _path.add(new Vector2(x, y));
    }
    
    for(; x < 240; x += 16) {
      _path.add(new Vector2(x, y));
      y -= 16;
    }
    
    for(; x < 560; x += 16) {
      _path.add(new Vector2(x, y));
    }
    
    System.out.println(_path.size());
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

  public void update(float delta) {
    if(delta >= 0.15f) {
      delta = 0.15f;
    }

    if(_init) {
      _init = false;
      _cornerDice = new SpinningDice[4];
      SpinningDice d = new SpinningDice(this, SpinningDice.Color.BLUE);
      d.setX(45);
      d.setY(45);
      addObject(d);
      _cornerDice[0] = d;
      
      d = new SpinningDice(this, SpinningDice.Color.WHITE);
      d.setX(9 * 128 + 45);
      d.setY(45);
      addObject(d);
      _cornerDice[1] = d;
            
      d = new SpinningDice(this, SpinningDice.Color.WHITE);
      d.setX(45);
      d.setY(4 * 128 + 45);
      addObject(d);
      _cornerDice[2] = d;
      
      d = new SpinningDice(this, SpinningDice.Color.BLUE);
      d.setX(9 * 128 + 45);
      d.setY(4 * 128 + 45);
      addObject(d);
      _cornerDice[3] = d;
      
      _cars = new Car[4];
      for(int f = 0; f < _cars.length; f++) {
        _cars[f] = new Car(this, (int)_path.get(0).x, (int)_path.get(0).y, 42, 28, f);
        addObject(_cars[f]);
      }
    }
    
    processFlungQueue();
    processObjects(delta);
    processCreatedObjects();
  }

  public void roll(int corner) {
    Random r = new Random();
    int value = r.nextInt(6) + 1;
    Dice d = new Dice(this, value, corner);
    d.setTargetPosition(_cars[corner]);
    addObject(d);
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
