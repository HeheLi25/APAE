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
	 * The inner class ClientThread.
	 * Each time when a client connected to the server,
	 * the server will create a ClientThread object to handle the client.
	 * So each ClientThread represents a client.
	 */
	private class ClientThread implements Runnable {
		private Socket s = null;
		private Server parent = null;
		private ObjectInputStream in = null;
		private ObjectOutputStream out = null;
		private String name;
		private ArrayList<Card> clientCards = new ArrayList<Card>();
		private int points = 0;
		/**
		 * Constructor of the ClientThread.
		 * @param s : The socket of this client. 
		 * @param parent : The parent server object.
		 */
		public ClientThread(Socket s, Server parent) {
			this.s = s;
			this.parent = parent;
			try {
				out = new ObjectOutputStream(this.s.getOutputStream());
				in = new ObjectInputStream(this.s.getInputStream());
			} catch (IOException e) {e.printStackTrace();}
		}
		/**
		 * This send method takes a package object and send it to this client.
		 * @param p The package to be sent.
		 */
		private void send(Package p) {
			System.out.println("Send package to "+name+": "+p.getType());
			try {
				out.writeObject(p);
			} catch (IOException e) {e.printStackTrace();}
		}
		/**
		 * This quit method delete the player from the waiting list or the player list,
		 * then update the displayed number of players.
		 * It is called when a client sent a QUIT package or disconnected.
		 */
		private void quit() {
			ArrayList<ClientThread> delList = new ArrayList<ClientThread>();
			delList.add(this);
			players.removeAll(delList);
			waitingPlayers.removeAll(delList);
			view.changeInAndWait(players.size(),waitingPlayers.size());		
			if(dealer == this) {
				dealer = null;
				view.writeLog("Dealer: " + name + " has quited. NO DEALER NOW.");
			}else view.writeLog("Player: " + name + " has quited.");
			statusCheck();
		}
		/**
		 * The run method of the ClientThread.
		 * When it start to run it keep on waiting for packages and deal with them.
		 */
		public void run() {
			System.out.println("Thread running");
			Package p;
			try {
				// Waiting for package from the client.
				while ((p = (Package) in.readObject()) != null) {
					System.out.println("Get package from "+name+": "+p.getType());
					/*	
					 * Handling QUIT package: 
					 *  call quit() for the player, then break the loop then the run() will end.
					 */
					if (p.getType().equals("QUIT")) {
						quit();
						break;
					}
					/*
					 * Handling REGISTER package:
					 * Record the client's name and add it the waiting list.
					 */
					if (p.getType().equals("REGISTER")) {
						name = (String) p.getObject();
						waitingPlayers.add(this);
						view.writeLog("Player: " + name + " is registered and waiting.");
						view.changeWait(waitingPlayers.size());						
						statusCheck();
					}
					/*
					 * Handling DRAW package:
					 * Call the cardToOne method to draw and send a card to this player.
					 */
					if (p.getType().equals("DRAW")) {
						cardToOne(this);
					}
					/*
					 * Handling PASS package:
					 * Update the passCounter variable and check whether all clients have chosen to pass.
					 */
					if (p.getType().equals("PASS")) {
						if(dealer == this) {
							clearingResult();
							continue;
						}
						passCounter ++;
						view.writeLog(name + " stop drawing cards (pass).");
						if(passCounter >= players.size()-1) {
							dealerTurn();	//If all remaining clients choose to pass, start the dealer's turn.
						}
					}
					/*
					 * Handling OUT package:
					 * A client sends OUT means he got more than 21 points. Move him from player list to waiting list.
					 */
					if (p.getType().equals("OUT")) {
						if(dealer == this) {
							dealerOut();
							continue;
						}
						ArrayList<ClientThread> delList = new ArrayList<ClientThread>();
						delList.add(this);
						players.removeAll(delList);
						waitingPlayers.add(this);
						view.writeLog(name + " is OUT of the game.");
						view.changeInAndWait(players.size(), waitingPlayers.size());
						//Inform the dealer that this player explodes. 
						dealer.send(new Package("EXPLODE",name));
						if(players.size() == 1) {
							dealer.send(new Package("MESSAGE","All other players are out of game. Congrates!"));
							dealer.send(new Package("END",0));
							endGame();
							continue;
						}
						// Since the size of player list changed, check again whether all remain players have chosen to pass. 
						if(passCounter >= players.size()-1) {	
							dealerTurn();
						}
					}	
				}
			} catch (SocketException e) {
				quit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}// The end of the inner class ClientThread.
	
	// Attributes of the Server itself.
	private int PORT = 8765;
	private ServerSocket server;
	private ServerView view;
	private ArrayList<ClientThread> players = new ArrayList<ClientThread>();
	private ArrayList<ClientThread> waitingPlayers = new ArrayList<ClientThread>();
	private int passCounter = 0; // This counter is to monitor whether all the players have chosen "pass" and the game shall end. 
	private String status = "waiting"; // The server has statuses: waiting/ready/start to control the availability of "start" button. 
	private ClientThread dealer = null;
	private int winners = 0;
	private int losers = 0;
	private CardDeck deck = new CardDeck();
	/**
	 * This StatusCheck method check the status of the game and the number of players waiting.
	 * The availability of start button will be changed if the status should change.
	 * Is called every time when a client has connected or quit, and when game starts. 
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
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		view = new ServerView(this);
		view.setVisible(true);
	}
	/**
	 * The run method of the server is always waiting for a new client connection.
	 */
	public void run() {
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = server.accept();
				view.writeLog("New client connected");
				ClientThread client = new ClientThread(clientSocket, this);
				(new Thread(client)).start();
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	/**
	 * This messageToAll method send a MESSAGE package to all the players in the player list.
	 * It is called when a message should be send to everyone.
	 * @param s : The String message that shall be sent to all players. 
	 */
	public void messageToAll(String s) {
		for(ClientThread player: players) {
			player.send(new Package("MESSAGE",s));
		}
		
	}
	/**
	 * The cleanToAll method send a CLEAN package to all players in the player list.
	 * To inform them that a new round started and their card deck should be clean.
	 */
	public void cleanToAll() {
		for(ClientThread player: players) {
			player.send(new Package("CLEAN",null));
		}
	}
	/**
	 * The cardToOne method draws a card and sends it to a player.
	 * @param player : The destination player of sending the card.
	 */
	public void cardToOne(ClientThread player) {
		Card thisCard = deck.draw();
		player.clientCards.add(thisCard);
		player.send(new Package("CARD",thisCard));
		view.writeLog(player.name +" draws: " + thisCard.toString());
	}
	/**
	 * The cardToAll method calls the cardToOne method several times to send a card to every player.
	 * The card drawing method is synchronized. The sleeping is only used to make it seems more real.
	 */
	public void cardToAll() {
		for(ClientThread player: players) {
			cardToOne(player);
			try {Thread.sleep(200);}catch(Exception exc) {exc.printStackTrace();}
		}
	}
	/**
	 * The askToAll method send a ASK package to all the players except the dealer.
	 * Instead it send the dealer a message to inform him to wait for other players.
	 */
	public void askToAll() {
		for(ClientThread player: players) {
			if(player != dealer)
				player.send(new Package("ASK",null));
			else {
				dealer.send(new Package("MESSAGE","As dealer, you'll wait for other players..."));
			}
		}
	}
	/**
	 * This countAllPoints method counts the points of each players using their cards and stores them.
	 * It calls PointCounter.countPoint().
	 */
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
	public boolean checkNatural21() {
		ArrayList<ClientThread> NVU = new ArrayList<ClientThread>();
		for(ClientThread player: players) 
			if(player.points == 21) 
				NVU.add(player);
		if(NVU.size() == 1) {	//One player wins with natural vingt-un (21).
			ClientThread winner = NVU.get(0);
			//Change the dealer to the winner
			view.writeLog("Dealer changed from " + dealer.name + " to " + winner.name);
			dealer.send(new Package("DEALER",false)); 
			dealer = winner; 
			dealer.send(new Package("DEALER",true));
			view.writeLog("----"+winner.name + " wins with natural 21!----");
			for(ClientThread player: players) {	//Send all losing players the message and change their stacks.  
				player.send(new Package("MESSAGE",winner.name+" has natural 21! You lose 2 stacks."));
				player.send(new Package("END",-2));
				if(player == winner) {	//Send the winner the different message and change the stacks
					int winStacks = 2*(players.size());
					winner.send(new Package("MESSAGE","YOU win with natural 21! You win "+winStacks+" stacks"));
					winner.send(new Package("END",winStacks));
				}
			}
			return true;
		}else if(NVU.size() > 1) {
			view.writeLog("----More than one natural 21. No winners.----");
			messageToAll("More than one natural 21. No winners.");
			for(ClientThread player: players) 
				player.send(new Package("END",0));
			return true;
		}else{
			view.writeLog("No natural 21.");	
			return false;
		}
	}
	/**
	 * The dealerTurn method is called when other players are all pass or out and it comes to the dealer's turn.
	 * It send an ASK package to the dealer.
	 */
	public void dealerTurn() {
		view.writeLog("All players pass, dealer's turn.");
		dealer.send(new Package("MESSAGE","Now is your turn."));
		dealer.send(new Package("ASK",null));
	}
	/**
	 * The dealerOut method handles the situation 
	 * that the dealer is out because of points over 21.
	 */
	public void dealerOut() {
		view.writeLog("Dealer: "+PointCounter.countPoint(dealer.clientCards)+", DEALER OUT!");
		int dealerLosingStack = 0;
		for(ClientThread player:players) {
			if(player != dealer) {
				player.send(new Package("MESSAGE","The dealer's points is over 21. You win a stack!"));
				player.send(new Package("END",1));
				dealerLosingStack++;
			}
		}
		int dealerStackChange = losers - dealerLosingStack;
		dealer.send(new Package("MESSAGE","Over 21! You lose "+dealerLosingStack+" stacks. "));	
		dealer.send(new Package("END",dealerStackChange+1));	
		endGame();
	}
	/**
	 * This clearingResult is used to deal the final result of a round of the game.
	 * It is called when the round naturally ends.
	 * It generate and send the result to all players.
	 */
	public void clearingResult() {
		view.writeLog("----All players have stopped drawing cards.----");
		countAllPoints();
		int dealerPoints = dealer.points;
		view.writeLog("The dealer (" + dealer.name +") got "+ dealerPoints+" points.");
		for(ClientThread player: players) {
			if(player != dealer) {
				if(dealerPoints > player.points) {	//Losers.
					player.send(new Package("MESSAGE","Dealer: "+dealerPoints +", you: "+player.points+". You lose a stack."));
					player.send(new Package("END",-1));
					losers ++;
				}else if(dealerPoints < player.points){	//Winners.
					player.send(new Package("MESSAGE","Dealer: "+dealerPoints +", you: "+player.points+". You win a stack."));
					player.send(new Package("END",1));
					winners ++;
				}else {	//Those who have same points with dealer.
					player.send(new Package("MESSAGE","Dealer: "+dealerPoints +", you: "+player.points+". Your stacks remains."));
					player.send(new Package("END",0));
				}
			}
		}
		//Deal with the dealer.
		int dealerStackChange = losers - winners;
		view.writeLog("The dealer wins "+ dealerStackChange +"stack(s).");
		dealer.send(new Package("MESSAGE",winners +" player defeat you, "+losers+" lose. Stack change: "+dealerStackChange));
		dealer.send(new Package("END", dealerStackChange));
		endGame();
	}
	/**
	 * The endGame method ends the game and initialize the ClientThreads and the server.
	 * It is called by other methods after they deal with each kind of results of a round.
	 */
	public void endGame() {
		waitingPlayers.addAll(players);
		players.clear();
		for(ClientThread player: waitingPlayers) {
			player.points = 0;
			player.clientCards.clear();
		}
		winners = 0;
		losers = 0;
		passCounter = 0;
		deck.init();
		view.changeInAndWait(players.size(), waitingPlayers.size());
		view.writeLog("-------------GAME END--------------");
		status = "waiting";
		statusCheck();
	}
	/**
	 * The actionPerformed method reacts to the start button and start a game.
	 * It creates and execute the GameWorker (SwingWorker) that proceed a round of the game.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == view.getStart()) { 
			GameWorker gw = new GameWorker();
			gw.execute();
		}
	}
	/**
	 * Inner class GameWorker which is a swingWorker.
	 */
	private class GameWorker extends SwingWorker<Void, Void>{
		/**
		 * The doInBackground method proceed a whole round of the game.
		 * It calls the methods defined above.
		 * It is triggered by the actionPerformed method of Server.
		 */
		public Void doInBackground(){
			//Move all clients in the waiting list to the player list.
			players.addAll(waitingPlayers);
			waitingPlayers.clear();
			cleanToAll();
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
							player.send(new Package("DEALER",true)); //Send a dealer package to inform the dealer.
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
			try {Thread.sleep(1000);}catch(Exception exc) {exc.printStackTrace();}
			countAllPoints();
			if(checkNatural21()) {
				endGame();
				return null;
			}
			askToAll();
			return null;
		}		
	}
	/**
	 * Main method to start the server. 
	 * @param args
	 */
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
