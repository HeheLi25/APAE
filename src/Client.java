import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.SwingConstants;

public class Client extends JFrame implements ActionListener{
	private JLabel text1,text2,text3,stack,dealer;
	private JButton btnquit,draw,pass;
	public JPanel cardPanel;
	
	private Socket server = null;
	private static String IP = "127.0.0.1";
	private static int PORT = 8765;
	private ObjectOutputStream out;
	private ReceiveWorker receiver;
	private boolean quit = false;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private int points;
	private int stacks = 10;
	private String name;
	
	public void setStateToWait() {
		text2.setText("Waiting for a new game to start...");
	}
	public void yourTurn() {
		draw.setEnabled(true);
		pass.setEnabled(true);
	}
	public void passYourTurn() {
		draw.setEnabled(false);
		pass.setEnabled(false);
	}
	public Client() {
		//The GUI page design.
		setTitle("Game Twenty-one");
		setSize(404, 300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		text1 = new JLabel("Username: ");
		text1.setFont(new Font("Candara", Font.PLAIN, 15));
		text1.setBounds(10, 0, 226, 23);
		text2 = new JLabel("Connecting to the server...");
		text2.setHorizontalAlignment(SwingConstants.CENTER);
		text2.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 17));
		text2.setBounds(0, 31, 384, 29);
		getContentPane().setLayout(null);
		getContentPane().add(text1);
		getContentPane().add(text2);
		dealer = new JLabel("Dealer");
		dealer.setFont(new Font("Ink Free", Font.BOLD, 20));
		dealer.setForeground(new Color(204, 102, 0));
		dealer.setHorizontalAlignment(SwingConstants.CENTER);
		dealer.setBounds(257, 72, 108, 29);
		dealer.setVisible(false);
		getContentPane().add(dealer);
		stack = new JLabel("Stack:"+stacks);
		stack.setFont(new Font("Candara", Font.PLAIN, 15));
		stack.setHorizontalAlignment(SwingConstants.TRAILING);
		stack.setBounds(257, 0, 120, 23);
		getContentPane().add(stack);
		cardPanel = new JPanel();
		cardPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		cardPanel.setBounds(10, 81, 242, 170);
		getContentPane().add(cardPanel);
		cardPanel.setLayout(new GridLayout(0,1));	
		draw = new JButton("Draw a card");
		draw.setFont(new Font("Impact", Font.PLAIN, 15));
		draw.setBounds(257, 111, 120, 40);
		draw.addActionListener(this);
		draw.setEnabled(false);
		getContentPane().add(draw);
		pass = new JButton("Pass");
		pass.setFont(new Font("Impact", Font.PLAIN, 15));
		pass.setBounds(257, 161, 120, 40);
		pass.setEnabled(false);
		pass.addActionListener(this);
		getContentPane().add(pass);	
		btnquit = new JButton("Quit");
		btnquit.setFont(new Font("Impact", Font.PLAIN, 15));
		btnquit.setBounds(257, 211, 120, 40);
		btnquit.addActionListener(this);
		getContentPane().add(btnquit);	
		text3 = new JLabel("Welcome.");
		text3.setFont(new Font("Candara", Font.BOLD, 15));
		text3.setBounds(10, 60, 242, 23);
		getContentPane().add(text3);
		setVisible(true);
		//Connect the server.
		try {
			server = new Socket(IP, PORT);
			out = new ObjectOutputStream(server.getOutputStream());
			receiver = new ReceiveWorker(server, this);
			receiver.execute();
		} catch (IOException e) {
			text2.setText("Connection failed.");
			e.printStackTrace();
			return;
		}
		/*
		 * When the client is connected to the server, show a dialog
		 * The user will input their name and it will be sent to the server. 
		 */
		String s = null;
		while (s == null||s.equals(""))
			s = JOptionPane.showInputDialog("Please input your name:");
		send(new Package("REGISTER", s));
		text1.setText("Username: "+s);
	
		name = s;
		setStateToWait();
	}
	/**
	 * The explode method deals the situation that the player's point is over 21.
	 * It will end the game for this client, and send an OUT package to inform the server.
	 */
	public void explode() {
		text2.setText(points + " is over 21! You lose 1 stack.");
		stacks = stacks - 1;
		stack.setText("Stacks: " + stacks);
		text3.setText("Waiting for a new game...");
		send(new Package("OUT", points));
		passYourTurn();
		btnquit.setEnabled(true);
		if(stacks <= 0) {
			text3.setText("You have been disconnected.");
			text2.setText("You don't have enough stack to start a game. ");
			send(new Package("QUIT",null));
			dealer.setVisible(false);
			cardPanel.setVisible(false);
		}
	}
	/**
	 * The actionPerformed method handles the clicks of the buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		//Send a QUIT package when quit button is pressed.
		if(e.getSource() == btnquit) {
			try {
				send(new Package("QUIT",null));
			}catch(Exception exc) {}
			quit = true;
			System.exit(0);
		}
		//Send a DRAW package when draw button is clicked.
		if(e.getSource() == draw) {
			send(new Package("DRAW",null));
		}
		//Send a PASS package when the pass button is clicked.
		if(e.getSource() == pass) {
			send(new Package("PASS",null));
			text2.setText("Waiting for the dealer...");
			passYourTurn();			
		}
	}
	/**
	 * The send method takes a package and send it to the server.
	 * @param p : The package to be sent.
	 */
	private void send(Package p) {
		System.out.println(name + " send package: " + p.getType());
		try {
			out.writeObject(p);
		} catch (IOException e) {e.printStackTrace();}
	}
	/**
	 * Inner class: ReceiveWorker.
	 * A SwingWorker that keeps waiting for package from the server,
	 * reacts and updates the frame.
	 */
	private class ReceiveWorker extends SwingWorker<Void, Void>{
		private ObjectInputStream in = null;
		private Socket socket = null;
		private Client parent;
		//Constructor
		public ReceiveWorker(Socket socket, Client parent) {
			this.socket = socket;
			this.parent = parent;
			try {
				in = new ObjectInputStream(this.socket.getInputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * The doInBackground method keeps waiting for package from the server,
		 * calls other methods, reacts and updates the frame.
		 */
		public Void doInBackground(){
			Package p = null;
			try {
				while ((p = (Package) in.readObject()) != null) {
					btnquit.setEnabled(false);
					System.out.println(name + " get package: " + p.getType());
					/*
					 * Received a DEALER package:
					 * That means this client has become the dealer. 
					 * Set the dealer signal to visible.
					 */
					if (p.getType().equals("DEALER")) {
						if((boolean)p.getObject()) dealer.setVisible(true);
						else dealer.setVisible(false);
					}
					/*
					 * Received a MESSAGE package:
					 * Show the message on text2.
					 */
					if (p.getType().equals("MESSAGE")) {
						text2.setText((String) p.getObject());
					}
					/*
					 * Received a CARD package:
					 * Add the card to the arrayList, then show it on the GUI.
					 */
					if (p.getType().equals("CARD")) {
						Card thisCard = (Card) p.getObject();
						cards.add(thisCard);
						JLabel cardLabel = new JLabel(thisCard.toString());
						cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
						cardLabel.setFont(new Font("Candara", Font.BOLD, 16));
						if(thisCard.getSuit().equals("Diamond")||thisCard.getSuit().equals("Heart"))cardLabel.setForeground(new Color(204, 0, 51));
						parent.cardPanel.add(cardLabel);
						//Update the total points.
						points = PointCounter.countPoint(cards);
						text3.setText("Points: "+ points);
						if(points>21) explode();
					}
					/*
					 * Received a CARD_DEALER package:
					 * This is the card drawn to choose a dealer, show it on text3 but not record it.
					 */
					if (p.getType().equals("CARD_DEALER")) {
						Card thisCard = (Card) p.getObject();
						cards.add(thisCard);
						text3.setText("You drawed: "+thisCard.toString());
					}
					/*
					 * Received a ASK package:
					 * This is the player's turn to choose to draw or pass. Show the message and enable the buttons.
					 */
					if (p.getType().equals("ASK")) {
						yourTurn();
						text2.setText("Please choose to DRAW or PASS.");
					}
					/*
					 * Received an END package:
					 * The round is ended, change the stack and text3.
					 */
					if (p.getType().equals("END")) {
						int stackChange = (int) p.getObject();
						stacks = stacks + stackChange;
						stack.setText("Stacks: "+stacks);
						text3.setText("Game ends. Waiting for a new game...");
						if(stacks <= 0) {
							text3.setText("You have been disconnected.");
							text2.setText("You don't have enough stack to start a game. ");
							send(new Package("QUIT",null));
							dealer.setVisible(false);
							cardPanel.setVisible(false);
						}
						btnquit.setEnabled(true);
					}
					/*
					 * Received a CLEAN package:
					 * Clear the cards in the cardPanel and the arrayList.
					 */
					if (p.getType().equals("CLEAN")) {
						cardPanel.removeAll();
						cards.clear();
						cardPanel.setVisible(false);
						text3.setText("Cleaning deck...");
						cardPanel.setVisible(true);
						
					}
					/*
					 * Received a EXPLODE package:
					 * This will only be received by dealer.
					 * Add one stack and show the name of the exploded player.
					 */
					if(p.getType().equals("EXPLODE")) {
						String explodePlayer = (String)p.getObject();
						text2.setText("Player "+ explodePlayer + " is out, your stack +1. ");
						stacks ++;
						stack.setText("Stacks: "+stacks);
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}	
	}
	/**
	 * Main method to start a client.
	 * @param args
	 */
	public static void main(String[] args) {
		Client c = new Client();
		
	}
}
