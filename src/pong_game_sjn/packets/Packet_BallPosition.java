package pong_game_sjn.packets;

import com.wirsching.net.packets.Packet;

import pong_game_sjn.Ball;
import pong_game_sjn.MainGame;

/**
 * This packet sends the ball position to the client. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-04-07
 */
public class Packet_BallPosition extends Packet {

	public Packet_BallPosition(Ball ball) {
		setID("ball_pos");
		putData("x", ball.getX());
		putData("y", ball.getY());
		setIP(MainGame.getConnectedClient().ip);
		setPort(MainGame.getConnectedClient().port);
	}
	
}
