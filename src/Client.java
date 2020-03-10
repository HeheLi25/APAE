import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket server;
	private static String IP = "127.0.0.1";
	private static int PORT = 8765;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private class Receiver implements Runnable{
		public void run() {
			
						
		}
	}
	
	private void send(Package p) {
		
	}

	public static void main(String[] args) {
		server = null;
		try {
			server = new Socket(IP,PORT);
			out = new ObjectOutputStream(server.getOutputStream());
			in = new ObjectInputStream(server.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
