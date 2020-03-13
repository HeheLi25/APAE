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
	private JLabel text1,text2,text3,stack;
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
	private int stacks = 100;
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
		setSize(440, 360);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		text1 = new JLabel("Username: ");
		text1.setBounds(21, 0, 226, 23);
		text2 = new JLabel("Connecting to the server...");
		text2.setHorizontalAlignment(SwingConstants.CENTER);
		text2.setFont(new Font("Arial", Font.BOLD, 13));
		text2.setBounds(0, 33, 403, 29);
		getContentPane().setLayout(null);
		getContentPane().add(text1);
		getContentPane().add(text2);
		stack = new JLabel("Stack:"+stacks);
		stack.setHorizontalAlignment(SwingConstants.TRAILING);
		stack.setBounds(266, 0, 137, 23);
		getContentPane().add(stack);
		cardPanel = new JPanel();
		cardPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		cardPanel.setBounds(20, 94, 242, 191);
		getContentPane().add(cardPanel);
		cardPanel.setLayout(new GridLayout(0,1));	
		draw = new JButton("Draw a card");
		draw.setBounds(283, 123, 120, 40);
		draw.addActionListener(this);
		draw.setEnabled(false);
		getContentPane().add(draw);
		pass = new JButton("Pass");
		pass.setBounds(283, 184, 120, 40);
		pass.setEnabled(false);
		pass.addActionListener(this);
		getContentPane().add(pass);	
		btnquit = new JButton("Quit");
		btnquit.setBounds(283, 245, 120, 40);
		btnquit.addActionListener(this);
		getContentPane().add(btnquit);	
		text3 = new JLabel("Welcome.");
		text3.setBounds(21, 71, 168, 23);
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
	public void explode() {
		text2.setText(points + " is over 21! You lose 1 stack.");
		stacks = stacks - 1;
		stack.setText("Stacks: " + stacks);
		text3.setText("Waiting for a new game...");
		send(new Package("OUT", points));
		passYourTurn();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnquit) {
			try {
				send(new Package("QUIT",null));
			}catch(Exception exc) {}
			quit = true;
			System.exit(0);
		}
		if(e.getSource() == draw) {
			send(new Package("DRAW",null));
		}
		if(e.getSource() == pass) {
			send(new Package("PASS",null));
			text2.setText("Waiting for the dealer...");
			passYourTurn();
			
		}
		
	}

	
	private void send(Package p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {e.printStackTrace();}
		System.out.println(name+": "+p.getType()+" sent.");
	}
	
	private class ReceiveWorker extends SwingWorker<Void, Void>{
		private ObjectInputStream in = null;
		private Socket socket = null;
		private Client parent;
		public ReceiveWorker(Socket socket, Client parent) {
			this.socket = socket;
			this.parent = parent;
			try {
				in = new ObjectInputStream(this.socket.getInputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		public Void doInBackground(){
			Package p = null;
			try {
				while ((p = (Package) in.readObject()) != null) {
					/*
					 * 
					 */
					if (p.getType().equals("MESSAGE")) {
						text2.setText((String) p.getObject());
					}
					/*
					 * 
					 */
					if (p.getType().equals("CARD")) {
						Card thisCard = (Card) p.getObject();
						cards.add(thisCard);
						JLabel cardLabel = new JLabel(thisCard.toString());
						cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
						parent.cardPanel.add(cardLabel);
						//Update the total points.
						points = PointCounter.countPoint(cards);
						text3.setText("Points: "+ points);
						if(points>21) explode();
					}
					if (p.getType().equals("CARD_DEALER")) {
						Card thisCard = (Card) p.getObject();
						cards.add(thisCard);
						text3.setText("You drawed: "+thisCard.toString());
					}
					if (p.getType().equals("ASK")) {
						yourTurn();
						text2.setText("Please choose to DRAW or PASS.");
					}
					if (p.getType().equals("END")) {
						int stackChange = (int) p.getObject();
						stacks = stacks + stackChange;
						stack.setText("Stacks: "+stacks);
						text3.setText("Waiting for a new game...");
					}
					
					/*
					 * 
					 */
					if (p.getType().equals("CLEAN")) {
						cardPanel.removeAll();
						cards.clear();
						text3.setText("Cleaning deck...");
					}
					/*
					 * 
					 */

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

	public static void main(String[] args) {
		Client c = new Client();
		
	}
}
