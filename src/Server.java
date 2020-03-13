import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

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
		private ArrayList<Card> clientCards = new ArrayList<Card>();
		private int points = 0;

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
			statusCheck();
		}

		public void run() {
			System.out.println("Thread running");
			Package p;
			try {
				// Waiting for package from the client.
				while ((p = (Package) in.readObject()) != null) {
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
	//Constructor
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
	public void messageToAll(String s) {
		for(ClientThread player: players) {
			player.send(new Package("MESSAGE",s));
		}
	}
	public void cleanToAll() {
		for(ClientThread player: players) {
			player.send(new Package("CLEAN",null));
		}
	}
	public void cardToAll() {
		for(ClientThread player: players) {
			Card thisCard = deck.draw();
			player.clientCards.add(thisCard);
			player.send(new Package("CARD",thisCard));
			view.writeLog(player.name +" draws: " + thisCard.toString());
			try {Thread.sleep(200);}catch(Exception exc) {exc.printStackTrace();}
		}
	}
	public void countAllPoints() {
		for(ClientThread player: players) {
			player.points = PointCounter.countPoint(player.clientCards);
			view.writeLog(player.name+" : "+player.points);
		}
	}
	/**
	 * This checkNVU method is called when the players got their first 2 cards
	 * to check whether there is one or more "Natural Vingt-Un".
	 */
	public void checkNatural21() {
		ArrayList<ClientThread> NVU = new ArrayList<ClientThread>();
		for(ClientThread player: players) 
			if(player.points == 21) {
				NVU.add(player);
			}
		if(NVU.size() == 1) {	//One player wins with natural vingt-un (21).
			ClientThread winner = NVU.get(0);
			view.writeLog("----"+winner.name + " wins with natural 21!----");
			for(ClientThread player: players) {	//Send all losing players the message and change their stacks.  
				player.send(new Package("MESSAGE",winner.name+" has natural 21! You lose 2 stacks."));
				player.send(new Package("END",-2));
				if(player == winner) {	//Send the winner the different message and change the stacks
					winner.send(new Package("MESSAGE","YOU win with natural 21! +"+2*(players.size()-1)+"stacks"));
					winner.send(new Package("END",2*(players.size()-1)));
				}
			}
			dealer = winner; //Change the dealer to the winner
			endGame();
		}else if(NVU.size() > 1) {
			view.writeLog("----More than one natural 21. No winners.----");
			messageToAll("More than one natural 21. No winners.");
			for(ClientThread player: players) {
				player.send(new Package("END",0));
			}
			endGame();
		}else view.writeLog("No natural 21.");	
	}
	
	
	public void endGame() {
		
	}
	
	

	public void actionPerformed(ActionEvent e) {
		/*
		 * The server pressed start. 
		 * Start the SwingWorker thread.
		 */
		if(e.getSource() == view.getStart()) { 
			GameWorker gw = new GameWorker();
			gw.execute();
		}
		
	}
	private class GameWorker extends SwingWorker<Void, Void>{
		public Void doInBackground(){
			//Move all clients in the waiting list to the player list.
			players.addAll(waitingPlayers);
			waitingPlayers.clear();
			view.changeInAndWait(players.size(),waitingPlayers.size());
			view.writeLog("Server has started the game.");
			view.writeLog("-------------GAME START--------------");
			//Send a START package to every player.
			messageToAll("GAME START!");
			view.writeLog("The START signal is sent to all clients.");
			status = "start";
			statusCheck();
			deck.init();
			try {Thread.sleep(1000);}catch(Exception exc) {exc.printStackTrace();}
			if(dealer == null) { 
				/*
				 * If there is no dealer, draw cards to choose one. 
				 * Send a package to signal that the choosing dealer process begins.
				 */
				messageToAll("Drawing cards to decide the dealer.");
				try {Thread.sleep(1000);}catch(Exception exc) {exc.printStackTrace();}
				view.writeLog("---Decide the dealer---");
				//Each player draws cards to choose a dealer.
				boolean dealerChosen = false;
				while(!dealerChosen) {
					for(ClientThread player: players) {
						Card thisCard = deck.draw();
						player.send(new Package("CARD_DEALER",thisCard));
						view.writeLog(player.name +" draws: " + thisCard.toString());
						try {Thread.sleep(200);}catch(Exception exc) {exc.printStackTrace();}
						//If any one draw an "A", the process is finished.
						if(thisCard.getNum().equals("A")) {
							dealerChosen = true;
							dealer = player;
							break;
						}
					}
				}
			}
			view.writeLog("---The dealer is: " + dealer.name + "---");
			for(ClientThread player: players) {
				if(player == dealer) {
					player.send(new Package("MESSAGE","YOU are the dealer now!"));
				}else {
					player.send(new Package("MESSAGE","The dealer is: "+dealer.name+". "));
				}
			}
			try {Thread.sleep(2000);}catch(Exception exc) {exc.printStackTrace();}
			/*
			 * Now the dealer is decided, the game starts.
			 * Send a clean signal to the clients so they can clean their card deck.
			 * Deal 2 cards for each player.
			 */
			cleanToAll();
			view.writeLog("Clients clean their decks.");
			messageToAll("Round start. Dealing cards.");
			try {Thread.sleep(1000);}catch(Exception exc) {exc.printStackTrace();}
			deck.init();
			//Two cards to each player. 
			cardToAll();
			cardToAll();
			messageToAll("Cards dealed.");
			view.writeLog("Cards dealed.");
			
			return null;
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
