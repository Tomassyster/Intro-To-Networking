package pong_game_sjn.packets;

import com.wirsching.net.packets.Packet;

import pong_game_sjn.MainGame;

/**
 * This packet sends the current score to the client. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-04-07
 */
public class Packet_Score extends Packet {

	public Packet_Score(int server_score, int client_score) {
		setID("score");
		putData("server_score", server_score);
		putData("client_score", client_score);
		setIP(MainGame.getConnectedClient().ip);
		setPort(MainGame.getConnectedClient().port);
	}
	
}
