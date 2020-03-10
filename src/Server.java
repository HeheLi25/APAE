import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
	private int PORT = 8765;
	private CardDeck deck = new CardDeck();
	
	private class ClientThread implements Runnable{
		 private Socket s = null;
	        private Server parent = null;
	        private ObjectInputStream inputStream = null;
	        private ObjectOutputStream outputStream = null;
	        public ClientThread(Socket s,Server parent) {
	            this.s = s;
	            this.parent = parent;
//	            try {
//	                outputStream = new ObjectOutputStream(this.s.getOutputStream());
//	                inputStream = new ObjectInputStream(this.s.getInputStream());
//	            }catch(IOException e) {
//	                e.printStackTrace();
//	            }
	        }
	        public void run() {

	        }
	    }
	
	    private ServerSocket server;
	    private ServerView view;
	    private ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	    public Server() {
	        try {
	        	view = new ServerView();
	        	view.setVisible(true);
	            server = new ServerSocket(8765);
	        }catch(IOException e) {
	            e.printStackTrace();
	        }
	    }
	    public void run() {
	        while(true) {
	            Socket clientSocket = null;
	            try {
	                clientSocket = server.accept();
	                view.writeLog("New client connected");
	                view.addWait();
	                ClientThread client = new ClientThread(clientSocket,this);
	                clients.add(client);
	                //new Thread(client).start();
	            }catch(IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    public static void main(String[] args) {
	        Thread t = new Thread(new Server());
	        t.start();
	        try {
	            t.join();
	        }catch(InterruptedException e) {
	            e.printStackTrace();
	        }
	        
	    }
}
