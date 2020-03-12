import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * The main class of server.
 */

public class Server implements Runnable, ActionListener {

	/**
	 * The inner class ClientThread. Each time when a client connected to the server
	 * the server will create a ClientThread object to handle the client.
	 */
	private class ClientThread implements Runnable {
		private Socket s = null;
		private Server parent = null;
		private ObjectInputStream in = null;
		private ObjectOutputStream out = null;
		private String name;
		private ArrayList<Card> cards = new ArrayList<Card>();

		public ClientThread(Socket s, Server parent) {
			this.s = s;
			this.parent = parent;
			try {
				out = new ObjectOutputStream(this.s.getOutputStream());
				in = new ObjectInputStream(this.s.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * This send method takes a package object and send it to the client.
		 * 
		 * @param p The package to be sent.
		 */
		private void send(Package p) {
			try {
				out.writeObject(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void quit() {
			ArrayList<ClientThread> delList = new ArrayList<ClientThread>();
			delList.add(this);
			players.removeAll(delList);
			waitingPlayers.removeAll(delList);
			view.changeInAndWait(players.size(),waitingPlayers.size());
			view.writeLog("Player: " + name + " has quited.");
			if(dealer == this) dealer = null;
			//Print the current players on the screen.
//			view.writeLog("------players------");
//			view.writeLog("In-game players:");
//			for(ClientThread p:players) {view.writeLog("-"+p.name);}
//			view.writeLog("Waiting players:");
//			for(ClientThread wp:waitingPlayers) {view.writeLog("-"+wp.name);}
//			view.writeLog("----------------------");
			statusCheck();
		}

		public void run() {
			System.out.println("Thread running");
			Package p;
			try {
				// Waiting for package from the client.
				while ((p = (Package) in.readObject()) != null) {
//	        			System.out.println("Package get.");
					if (p.getType().equals("QUIT")) {
						quit();
						break;
					}
					if (p.getType().equals("REGISTER")) {
						name = (String) p.getObject();
						waitingPlayers.add(this);
						view.writeLog("Player: " + name + " is registered and waiting.");
						view.changeWait(waitingPlayers.size());						
						statusCheck();
					}
				}
			} catch (SocketException e) {
				quit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private int PORT = 8765;
	private ServerSocket server;
	private ServerView view;
	private ArrayList<ClientThread> players = new ArrayList<ClientThread>();
	private ArrayList<ClientThread> waitingPlayers = new ArrayList<ClientThread>();
	private ClientThread dealer = null;
	private String status = "waiting";
	private CardDeck deck = new CardDeck();
	//Statuses: waiting/ready/start
	
	/**
	 * This StatusCheck method check the status of the game and the number of players waiting.
	 * Reflecting the status to the view. 
	 */
	private void statusCheck() {
		if(status.equals("waiting")) {if(waitingPlayers.size()>1) status = "ready";}
		if(status.equals("ready")) {if(waitingPlayers.size()<2) status = "waiting";}
		if(status.equals("start")) {
			if(players.size()==0) status = "waiting";
		}
		view.changeStatus(status);
	}
	
	/**
	 * The refreshDeck method refresh the card deck.
	 * Used when a new game starts and the choosing dealer process ends.
	 */
	public void refreshDeck() {
		deck = new CardDeck();
		view.writeLog("The card deck is refreshed.");
	}	

	/**
	 * Constructor of the Server.
	 */
	public Server() {
		try {
			view = new ServerView(this);
			view.setVisible(true);
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The run method of the server always waiting for a new client connection
	 */
	public void run() {
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = server.accept();
				view.writeLog("New client connected");
				ClientThread client = new ClientThread(clientSocket, this);
				(new Thread(client)).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == view.getStart()) { 
			//The server pressed start. 
			//Move all the players in waiting list to the in-game list. 
			players.addAll(waitingPlayers);
			waitingPlayers.clear();
			view.changeIn(players.size());
			view.changeWait(waitingPlayers.size());
			view.writeLog("Server has started the game.");
			view.writeLog("-------------GAME START--------------");
			//Send a START package to every client.
			Package startPackage = new Package("MESSAGE","GAME START!");
			for(ClientThread player: players) {player.send(startPackage);}
			view.writeLog("The START signal is sent to all clients.");
			status = "start";
			statusCheck();
			refreshDeck(); 
			try {Thread.sleep(1000);}catch(Exception exc) {exc.printStackTrace();}
			if(dealer == null) { 
				//If there is no dealer, draw cards to choose one. 
				//Send a package to signal that the choosing dealer process begins.
				Package p = new Package("MESSAGE","Draw cards to decide the dealer!");
				view.writeLog("No dealer. Players draw to decide who is the dealer.");
				for(ClientThread player: players) {
					player.send(p);  
				}
				//Each player draws cards to choose a dealer.
				boolean dealerChosen = false;
				while(!dealerChosen) {
					for(ClientThread player: players) {
						Card thisCard = deck.draw();
						player.send(new Package("CARD",thisCard));
						view.writeLog(player.name +" draws: " + thisCard.toString());
						try {Thread.sleep(500);}catch(Exception exc) {exc.printStackTrace();}
						//If any one draw an "A", the process is finished.
						if(thisCard.getNum().equals("A")) {
							dealerChosen = true;
							dealer = player;
							break;
						}
					}
				}
				view.writeLog("The dealer is: "+ dealer.name);
				for(ClientThread player: players) {
					if(player == dealer) {
						player.send(new Package("MESSAGE","YOU are the dealer now!"));
					}else {
						player.send(new Package("MESSAGE","The dealer is: "+dealer.name+". "));
					}
				}
				
				
			}
			
		}
		
	}

	public static void main(String[] args) {
		Thread t = new Thread(new Server());
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}
