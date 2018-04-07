package pong_game_sjn.packets;

import com.wirsching.net.packets.Packet;

/**
 * This packet sends the client paddle position to the server. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-04-07
 */
public class Packet_ClientPaddlePos extends Packet {

	public Packet_ClientPaddlePos(float paddlePos) {
		setID("client_paddle_pos");
		putData("position", paddlePos);
	}
	
}
