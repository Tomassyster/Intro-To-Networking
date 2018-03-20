package ping_pong;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This Client will connect to a server and send the string "ping". <br>
 * 
 * @author Pontus Wirsching
 * @since 2018-03-20
 */
public class Client {

	/**
	 * The InetAddress to the Server. <br>
	 * Aka. the IP. <br>
	 */
	private static InetAddress ipAddress;
	
	/**
	 * The port to the Server. <br>
	 */
	private static int port = 1234;
	
	/**
	 * The Client socket. <br>
	 */
	private static DatagramSocket socket;
	
	
	public static void main(String[] args) {
		
		try {
			
			// Create a new DatagramSocket.
			socket = new DatagramSocket();
			
		} catch(SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			
			// Set the InetAddress to "localhost".
			ipAddress = InetAddress.getByName("localhost");
			
		} catch(UnknownHostException e) {
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
					
					// Print the incoming data from the Server.
					System.out.println("Server > " + new String(packet.getData()).trim());
					
				}
			}
			
		}).start();
		
		// Parse the String "ping" into an array of bytes.
		byte[] data = "ping".getBytes();
		
		// Create a DatagramPacket that contains the array of bytes.
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		
		try {
		
			// Use the socket to send the DatagramPacket to the Server.
			socket.send(packet);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
	}

}
