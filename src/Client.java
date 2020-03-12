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
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

public class Client extends JFrame implements ActionListener{
	private JLabel text1;
	private JLabel text2;
	private Client controller;
	private JButton btnquit,draw,pass;
	public JPanel cardPanel;
	
	private Socket server = null;
	private static String IP = "127.0.0.1";
	private static int PORT = 8765;
	private ObjectOutputStream out;
	private ReceiveWorker receiver;
	private boolean quit = false;
	private String name;
	
	public void setStateToWait() {
		text2.setText("Waiting for a new game to start...");
	}
	public void yourTurn() {
		draw.setEnabled(true);
		pass.setEnabled(true);
	}
	public Client() {
		setTitle("Game Twenty-one");
		setSize(400, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(500, 300);
		text1 = new JLabel("Username: ");
		text1.setFont(new Font("Arial", Font.BOLD, 15));
		text1.setBounds(21, 14, 233, 29);
		text2 = new JLabel("Connecting to the server...");
		text2.setFont(new Font("Arial", Font.BOLD, 15));
		text2.setBounds(21, 49, 332, 29);
		getContentPane().setLayout(null);
		getContentPane().add(text1);
		getContentPane().add(text2);
		
		cardPanel = new JPanel();
		cardPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		cardPanel.setBounds(21, 107, 342, 178);
		getContentPane().add(cardPanel);
		cardPanel.setLayout(new GridLayout(0,1));
		
		draw = new JButton("Draw a card");
		draw.setBounds(53, 300, 120, 40);
		draw.setEnabled(false);
		getContentPane().add(draw);
		pass = new JButton("Pass");
		pass.setBounds(210, 300, 120, 40);
		pass.setEnabled(false);
		getContentPane().add(pass);
		
		btnquit = new JButton("Quit");
		btnquit.setBounds(285, 10, 78, 29);
		btnquit.addActionListener(controller);
		getContentPane().add(btnquit);
		
		JLabel yourCards = new JLabel("Your cards");
		yourCards.setBounds(21, 83, 108, 23);
		yourCards.setFont(new Font("Arial", Font.BOLD, 12));
		getContentPane().add(yourCards);
		
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
		this.name = s;
		text1.setText("Username: "+s);
		setStateToWait();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnquit) {
			try {
				send(new Package("QUIT",null));
			}catch(Exception exc) {
//				exc.printStackTrace();
			}
			quit = true;
			System.exit(0);
		}
		
	}

	
	private void send(Package p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {e.printStackTrace();}
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
					System.out.println("Package received");
					if (p.getType().equals("MESSAGE")) {
						text2.setText((String) p.getObject());
					}
					if (p.getType().equals("CARD")) {
						Card thisCard = (Card) p.getObject();
						System.out.println(thisCard.toString());
						text2.setText("1");
						parent.cardPanel.add(new JLabel(thisCard.toString()));
						
					}
					if (p.getType().equals("CLEAR")) {
						//view.getCardPanel().removeAll();
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

	public static void main(String[] args) {
		Client c = new Client();
		
	}

}