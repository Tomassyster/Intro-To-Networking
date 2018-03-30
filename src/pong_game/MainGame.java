package pong_game;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import com.heat.engine.Engine;
import com.heat.engine.game.Game;
import com.heat.engine.graphics.Graphics;
import com.heat.engine.input.Keyboard;

/**
 * This "Pong" game is meant as an example for a simple multiplayer game written in Java. <br>
 * The game itself uses the game engine "Heat Engine". All network code is written from scratch. <br>
 * 
 * Both the server and client is written in the same class and lets the user choose whether to host or join a game. <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-03-30
 *
 */
public class MainGame extends Game {
	
	class Client {
		public InetAddress ip;
		public int port;
	}
	
	// Save the connected clients information.
	private Client connected_client = null;
	
	// The client uses these to connect to the server.
	private InetAddress ipAddress = null;
	private int port = 0;

	// The two player paddles.
	public Paddle server_paddle = new Paddle("server_paddle");
	public Paddle client_paddle = new Paddle("client_paddle");
	
	// The ball instance.
	public Ball ball = new Ball();
	
	// The score board.
	private int server_score = 0;
	private int client_score = 0;
	
	// The socket used to establish a connection.
	private DatagramSocket socket;
	
	// Is this instance the host?
	private boolean isHosting = false;
	
	// Has the game started?
	private boolean gameStarted = false;
	
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
			
			try {
				
				// Create a new socket that listens on the selected port.
				socket = new DatagramSocket(port);
			} catch(SocketException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			// Create new thread to receive packets.
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (true) {
						
						byte[] data = new byte[1024];
						DatagramPacket packet = new DatagramPacket(data, data.length);
						
						try {
							
							// Wait for an incoming packet.
							socket.receive(packet);
							
						} catch(IOException e) {
							e.printStackTrace();
							System.exit(0);
						}
						
						// Split the message at "//".
						String[] splitted_message = new String(packet.getData()).trim().split("//");
						
						switch (splitted_message[0]) {
						
							// Save the connected clients information and send back the position of the server paddle.
							case "player_connect":
								
								// Print out that a player has connected. Just for debugging purposes.
								System.out.println("Player connected from '" + packet.getAddress().getHostName() + ":" + packet.getPort() + "'");
								
								// Initialize the connected_client and fill in the information.
								connected_client = new Client();
								connected_client.ip = packet.getAddress();
								connected_client.port = packet.getPort();
								
								// Send the server paddle position back to the client.
								sendString("server_paddle_pos//" + server_paddle.getY(), packet);
								break;
								
							// Sets the position of the client paddle.
							case "client_paddle_pos":
								client_paddle.setY(Float.parseFloat(splitted_message[1]));
								break;

							default:
								break;
						}
						
						
					}
				}
				
			}).start();	
			
		} else {
			
			// Let the user pick an IP address to connect to.
			String ip = JOptionPane.showInputDialog("Enter server address as: 'ip:port'", "localhost:1234");
			
			// Split the ip and port
			String[] address = ip.split(":");
			port = Integer.parseInt(address[1]);
			try {
				ipAddress = InetAddress.getByName(address[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			
			try {
			
				// Create a new socket.
				socket = new DatagramSocket();
			} catch(SocketException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			
			// Create new thread to receive packets.
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (true) {
						
						byte[] data = new byte[1024];
						DatagramPacket packet = new DatagramPacket(data, data.length);
						
						try {
							
							// Wait for an incoming packet.
							socket.receive(packet);
							
						} catch(IOException e) {
							e.printStackTrace();
							System.exit(0);
						}
						
						// Split the message at "//".
						String[] splitted_message = new String(packet.getData()).trim().split("//");
						
						switch (splitted_message[0]) {
							
							// Sets the position of the server paddle.
							case "server_paddle_pos":
								server_paddle.setY(Float.parseFloat(splitted_message[1]));
								break;
								
							// Sets the position of the ball.
							case "ball_pos":
								ball.setX(Float.parseFloat(splitted_message[1]));
								ball.setY(Float.parseFloat(splitted_message[2]));
								break;
							
							// Update the score board for the client.
							case "score":
								server_score = Integer.parseInt(splitted_message[1]);
								client_score = Integer.parseInt(splitted_message[2]);
								break;

						default:
							break;
						}
						
						
					}
				}
				
			}).start();	
			
			// Send the 'player_connect' packet to the server.
			sendString("player_connect", ipAddress, port);
			
		}
		
		// Set the paddle positions.
		server_paddle.setX(50);
		client_paddle.setX(getWidth() - client_paddle.getWidth() - 50);
		
		// [Heat Engine] Start the game.
		start();
	}
	
	/**
	 * Sends a string to a specific InetAddress and port.
	 */
	public void sendString(String message, InetAddress address, int port) {
		byte[] data = message.getBytes();
		DatagramPacket p = new DatagramPacket(data, data.length, address, port);
		try {
			socket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Sends a message back to a received packet sender.
	 */
	public void sendString(String message, DatagramPacket packet) {
		sendString(message, packet.getAddress(), packet.getPort());
	}

	/**
	 * Sends the string to the connected client.
	 */
	public void sendString(String message) {
		if (connected_client != null)
			sendString(message, connected_client.ip, connected_client.port);
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
					sendString("server_paddle_pos//" + server_paddle.getY(), connected_client.ip, connected_client.port);
			} else {
				sendString("client_paddle_pos//" + client_paddle.getY(), ipAddress, port);
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
					Graphics.drawString("Client '" + connected_client.ip.getHostName() + ":" + connected_client.port + "' has connected!", getWidth() / 2 - 130, getHeight() * 0.7f);
				Graphics.drawString("Press [SPACE] to start!", getWidth() / 2 - 70, getHeight() * 0.7f + 20);
				
				// Start the game if the host is pressing space.
				if (Keyboard.isKeyPressed(KeyEvent.VK_SPACE)) {
					gameStarted = true;
					ball.launch();
					first = false;
				}
			}
			
			
			// Send the ball position.
			sendString("ball_pos//" + ball.getX() + "//" + ball.getY(), connected_client.ip, connected_client.port);
			
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
				sendString("score//" + server_score + "//" + client_score);
			}
			
			// The server scored!
			if (ball.getX() >= getWidth()) {
				server_score++;
				ball.reset();
				gameStarted = false;
				sendString("score//" + server_score + "//" + client_score);
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
