package pong_game_sjn.packets;

import com.wirsching.net.packets.Packet;

import pong_game_sjn.MainGame;

/**
 * This packet sends the servers paddle position to the client. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-04-07
 */
public class Packet_ServerPaddlePos extends Packet {

	public Packet_ServerPaddlePos(float position) {
		setID("server_paddle_pos");
		putData("position", position);
		setIP(MainGame.getConnectedClient().ip);
		setPort(MainGame.getConnectedClient().port);
	}
	
}
