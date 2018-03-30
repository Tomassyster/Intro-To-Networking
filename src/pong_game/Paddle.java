package pong_game;

import java.awt.Color;

import com.heat.engine.graphics.Graphics;
import com.heat.engine.math.Rectangle;

public class Paddle extends Rectangle {

	private String id = "undefined";
	
	public float speed = 400;
	
	public Paddle(String id) {
		this.id = id;
		setWidth(20);
		setHeight(150);
	}
	
	public void draw() {
		Graphics.setColor(Color.WHITE);
		Graphics.fillRect(this);
	}

}
