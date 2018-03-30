package pong_game;

import java.awt.Color;

import com.heat.engine.game.Game;
import com.heat.engine.graphics.Graphics;
import com.heat.engine.math.Rectangle;
import com.heat.engine.math.Vector;

public class Ball extends Rectangle {

	public Vector velocity = new Vector();
	
	public float speed = 200;
	
	public Ball() {
		
		float s = 25;
		
		setX(MainGame.getWidth() / 2 - s / 2);
		setY(MainGame.getHeight() / 2 - s / 2);
		
		setWidth(s);
		setHeight(s);
	}
	
	public void launch() {
		velocity.x = speed;
		velocity.y = -speed;
	}
	
	public void reset() {
		setX(Game.getWidth() / 2 - getWidth() / 2);
		setY(Game.getHeight() / 2 - getHeight() / 2);
		velocity = new Vector();
	}
	
	public void draw() {
		
		if (getY() <= 0 || getY() >= Game.getHeight() - getHeight()) {
			velocity.y = -velocity.y;
		}
		
		setX(getX() + velocity.getX() * Game.getDelta());
		setY(getY() + velocity.getY() * Game.getDelta());
		
		Graphics.setColor(Color.WHITE);
		Graphics.fillRect(this);
	}

}
