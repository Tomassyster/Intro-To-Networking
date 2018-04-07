package pong_game_sjn;

import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import com.heat.engine.Engine;
import com.heat.engine.game.Game;
import com.heat.engine.graphics.Graphics;
import com.heat.engine.input.Keyboard;
import com.wirsching.net.client.Client;
import com.wirsching.net.client.ConnectionHandler;
import com.wirsching.net.packets.Packet;
import com.wirsching.net.server.Server;

import pong_game_sjn.packets.Packet_BallPosition;
import pong_game_sjn.packets.Packet_ClientPaddlePos;
import pong_game_sjn.packets.Packet_Score;
import pong_game_sjn.packets.Packet_ServerPaddlePos;

/**
 * This is the exact same pong game from "pong_game" but the networking part is done using Simple Java Networking. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-04-07
 *
 */
public class MainGame extends Game {
	
	public class ClientInfo {
		public String ip;
		public int port;
	}
	
	// Save the connected clients information.
	private static ClientInfo connected_client = null;
	
	public static ClientInfo getConnectedClient() {
		return connected_client;
	}

	// The two player paddles.
	public Paddle server_paddle = new Paddle("server_paddle");
	public Paddle client_paddle = new Paddle("client_paddle");
	
	// The ball instance.
	public Ball ball = new Ball();
	
	// The score board.
	private int server_score = 0;
	private int client_score = 0;
	
	// Is this instance the host?
	private boolean isHosting = false;
	
	// Has the game started?
	private boolean gameStarted = false;
	
	private Server server = null;
	private Client client = null;
	
	public MainGame() {
		
		// [Heat Engine] Create the window.
		super(800, 600, "Multiplayer Pong");
		
		// [Heat Engine] Cap the FPS at 60.
		Engine.getRenderer().setFPScap(60);
		
		// [HEat Engine] The game does not support resizing.
		getWindow().setResizable(false);
		
		// Let the user choose to host or join a game.
		if (JOptionPane.showConfirmDialog(getWindow().getFrame(), "Do you want to host the game?") == 0) {
			
			// Set the isHosting to true.
			isHosting = true;
			
			// Let the user select a port to host the server on.
			int port = Integer.parseInt(JOptionPane.showInputDialog("Enter port", "1234"));
			
			// Create a new server instance.
			server = new Server(port);
			
			// Set the ConnectionHandler.
			server.setConnectionHandler(new ConnectionHandler() {
				
				@Override
				public void received(Packet packet) {
					
					if (packet.getID().equals("client_paddle_pos")) {
						client_paddle.setY(Float.parseFloat(packet.getValue("position")));
					}
					
				}
				
				@Override
				public void error(Packet packet) {
					
				}
				
				@Override
				public void disconnected(Packet packet) {
					
				}
				
				@Override
				public void connected(Packet packet) {
					// Print out that a player has connected. Just for debugging purposes.
					System.out.println("Player connected from '" + packet.getIP() + ":" + packet.getPort() + "'");
					
					// Initialize the connected_client and fill in the information.
					connected_client = new ClientInfo();
					connected_client.ip = packet.getIP();
					connected_client.port = packet.getPort();
					
					// Send the server paddle position back to the client.
					server.sendPacket(new Packet_ServerPaddlePos(server_paddle.getY()));
				}
				
			});
			
			server.start();
			
		} else {
			
			// Let the user pick an IP address to connect to.
			String ip = JOptionPane.showInputDialog("Enter server address as: 'ip:port'", "localhost:1234");
			
			// Split the ip and port
			String[] address = ip.split(":");
			int port = Integer.parseInt(address[1]);

			// Create a new instance of the client.
			client = new Client();
			
			// Set the ConnectionHandler.
			client.addConnectionHandler(new ConnectionHandler() {
				
				@Override
				public void received(Packet packet) {
					
					switch (packet.getID()) {
						// Sets the position of the server paddle.
						case "server_paddle_pos":
							server_paddle.setY(Float.parseFloat(packet.getValue("position")));
							break;
							
						// Sets the position of the ball.
						case "ball_pos":
							ball.setX(Float.parseFloat(packet.getValue("x")));
							ball.setY(Float.parseFloat(packet.getValue("y")));
							break;
						
						// Update the score board for the client.
						case "score":
							server_score = Integer.parseInt(packet.getValue("server_score"));
							client_score = Integer.parseInt(packet.getValue("client_score"));
							break;
	
						default:
							break;
					}
					
				}
				
				@Override
				public void error(Packet packet) {
					
				}
				
				@Override
				public void disconnected(Packet packet) {
					
				}
				
				@Override
				public void connected(Packet packet) {
					
				}

			});
			
			// Connect to the specified server.
			client.connectToServer(address[0], port);
			
		}
		
		// Set the paddle positions.
		server_paddle.setX(50);
		client_paddle.setX(getWidth() - client_paddle.getWidth() - 50);
		
		// [Heat Engine] Start the game.
		start();
	}
	
	// Is the ball touching a paddle?
	private boolean touchingPaddle = false;
	
	// Is this the first time the game is started?
	private boolean first = true;
	
	@Override
	public void draw() {
		
		// This boolean will be set to true if the player has moved.
		boolean moved = false;
		
		// Check for movement upwards.
		if (Keyboard.isKeyPressed(KeyEvent.VK_W)) {
			
			// Move the correct paddle depending on the client/server instance.
			if (isHosting) {
				server_paddle.setY(server_paddle.getY() - server_paddle.speed * getDelta());
				if (server_paddle.getY() <= 0) server_paddle.setY(0);
			} else {
				client_paddle.setY(client_paddle.getY() - client_paddle.speed * getDelta());
				if (client_paddle.getY() <= 0) client_paddle.setY(0);
			}
			moved = true;
		}
		
		// Check for movement downwards.
		if (Keyboard.isKeyPressed(KeyEvent.VK_S)) {
			
			// Move the correct paddle depending on the client/server instance.
			if (isHosting) {
				server_paddle.setY(server_paddle.getY() + server_paddle.speed * getDelta());
				if (server_paddle.getY() >= getHeight() - server_paddle.getHeight()) server_paddle.setY(getHeight() - server_paddle.getHeight());
			} else {
				client_paddle.setY(client_paddle.getY() + client_paddle.speed * getDelta());
				if (client_paddle.getY() >= getHeight() - client_paddle.getHeight()) client_paddle.setY(getHeight() - client_paddle.getHeight());
			}
			moved = true;
		}
		
		// If the player moved, send the new position to the other player.
		if (moved) {
			if (isHosting) {
				if (connected_client != null)
					server.sendPacket(new Packet_ServerPaddlePos(server_paddle.getY()));
			} else {
					client.sendPacket(new Packet_ClientPaddlePos(client_paddle.getY()));
			}
		}
		
		// Draw both paddles.
		server_paddle.draw();
		client_paddle.draw();
		
		// Draw the ball.
		ball.draw();
		
		
		// All code in here will only be executed if this instance is the host and another player is connected.
		if (isHosting && (connected_client != null)) {
			
					
			// Check if the game isn't started.
			if (!gameStarted) {
				
				if (first)
					Graphics.drawString("Client '" + connected_client.ip + ":" + connected_client.port + "' has connected!", getWidth() / 2 - 130, getHeight() * 0.7f);
				Graphics.drawString("Press [SPACE] to start!", getWidth() / 2 - 70, getHeight() * 0.7f + 20);
				
				// Start the game if the host is pressing space.
				if (Keyboard.isKeyPressed(KeyEvent.VK_SPACE)) {
					gameStarted = true;
					ball.launch();
					first = false;
				}
			}
			
			// Send the ball position.
			server.sendPacket(new Packet_BallPosition(ball));
			
			// Check if the client paddle intersects the ball.
			if (client_paddle.contains(ball) && !touchingPaddle) {
				ball.velocity.x = -ball.velocity.x;
				touchingPaddle = true;
			}
			
			// Check if the server paddle intersects the ball.
			if (server_paddle.contains(ball) && !touchingPaddle) {
				ball.velocity.x = -ball.velocity.x;
				touchingPaddle = true;
			}
			
			// Reset the touchingPaddle variable.
			if (!server_paddle.contains(ball) && !client_paddle.contains(ball) && touchingPaddle) touchingPaddle = false;
			
			
			// The client scored!
			if (ball.getX() <= -ball.getWidth()) {
				client_score++;
				ball.reset();
				gameStarted = false;
				server.sendPacket(new Packet_Score(server_score, client_score));
			}
			
			// The server scored!
			if (ball.getX() >= getWidth()) {
				server_score++;
				ball.reset();
				gameStarted = false;
				server.sendPacket(new Packet_Score(server_score, client_score));
			}
			
		}
		
		// If this instance is the host and there is no connected client then display this message.
		if (isHosting && (connected_client == null)) {
			Graphics.drawString("Waiting for client...", getWidth() / 2 - 50, getHeight() * 0.7f);
		}
		
		// Change the font and draw the score board.
		Graphics.getGraphics2D().setFont(new Font("Consolas", 0, 40));
		String serverScore = String.valueOf(server_score);
		Graphics.drawString(serverScore + " - " + client_score, getWidth() / 2 - 30 - serverScore.length() * 24, 60);
		
	}
	
	public static void main(String[] args) {
		
		// Create a new instance of this class and start the game.
		new MainGame();
	}

}
