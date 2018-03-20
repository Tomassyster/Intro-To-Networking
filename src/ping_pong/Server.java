package ping_pong;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * The Server will standby and wait for incoming messages from a Client. <br>
 * If the Client sends the String "ping", the Server responds with a "pong". <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-03-20
 *
 */
public class Server {
	
	/**
	 * The port that the Server will listen to. <br>
	 */
	private static int port = 1234;
	
	/**
	 * The Server socket. <br>
	 */
	private static DatagramSocket socket;
	
	
	public static void main(String[] args) {
		
		try {
			
			// Create a new DatagramSocket, because this is the server, the port has to be inserted here.
			socket = new DatagramSocket(port);
			
		} catch(SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// Create a new Thread to receive incoming data.
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					
					// Allocate memory for the incoming packet of data. In this example, limited to 1024 bytes.
					byte[] data = new byte[1024];
					
					// Create a new DatagramPacket to receive incoming data.
					DatagramPacket packet = new DatagramPacket(data, data.length);
					
					try {
						
						// Wait until the socket receives data.
						socket.receive(packet);
						
					} catch(IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
					
					// Print the incoming data from the Client.
					String message = new String(packet.getData()).trim();
					
					// Print the message.
					System.out.println("Client [" + packet.getAddress().getHostName() + ":" + packet.getPort() + "] > " + message);
					
					// Incase the message equals "ping", the server will respond with a "pong".
					if (message.equals("ping")) {
						
						// Parse the String "pong" into an array of bytes.
						byte[] dataSend = "pong".getBytes();
						
						// Create a DatagramPacket that contains the array of bytes.
						// As the server should return this message to the sender, it grabs the InetAddress and port from the received packet.
						DatagramPacket packetSend = new DatagramPacket(dataSend, dataSend.length, packet.getAddress(), packet.getPort());
						
						try {
						
							// Use the socket to send the DatagramPacket to the Server.
							socket.send(packetSend);
							
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(0);
						}
						
					}
					
				}
			}
			
		}).start();	
		
	}

}
