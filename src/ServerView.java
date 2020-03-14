import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Color;

/**
 * This is the view of the Server.
 */
public class ServerView extends JFrame{
	private JTextArea logField;
	private JButton start;
	private JLabel in;
	private JLabel wait;
	private Server controller;
	
	public JButton getStart() {
		return start;
	}
	/**
	 * This method changes the number of the in-game player displayed on the frame.
	 * @param i : number of in-game player.
	 */
	public void changeIn(int i) {
		in.setText(i+" player(s) in game.");
	}
	/**
	 * This method returns the current in-game player.
	 * @return : number of in-game player.
	 */
	public int getIn() {
		return Integer.parseInt(in.getText().split(" ")[0]+"");
	}
	/**
	 * This method changes the number of the waiting player displayed on the frame.
	 * @param i : number of waiting player.
	 */
	public void changeWait(int i) {
		wait.setText(i+" player(s) waiting.");
	}
	/**
	 * This method returns the current in-game player.
	 * @return : number of waiting player.
	 */
	public int getWait() {
		return Integer.parseInt(wait.getText().split(" ")[0]+"");
	}
	/**
	 * This method changes both the in-game player number and waiting player number
	 * display on the frame in the same time by calling the methods above.
	 * @param in : number of in-game player.
	 * @param wait: number of waiting player.
	 */
	public void changeInAndWait(int in, int wait) {
		changeIn(in);
		changeWait(wait);
	}
	/**
	 * This method takes a string and write it to the TextArea of the log.
	 * @param s: The String to write.
	 */
	public void writeLog(String s) {
		logField.append( s + "\n" );
	}
	//Constructor
	public ServerView(Server s) {
		controller = s;
		setTitle("Game Server");
		setSize(300,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		in = new JLabel("0 player(s) in game.");
		in.setFont(new Font("Candara", Font.BOLD, 16));
		in.setBounds(21, 10, 151, 29);
		wait = new JLabel("0 player(s) waiting.");
		wait.setFont(new Font("Candara", Font.BOLD, 15));
		wait.setBounds(21, 49, 151, 29);
		getContentPane().setLayout(null);
		getContentPane().add(in);
		getContentPane().add(wait);
		JLabel logTitle = new JLabel("Running log:");
		logTitle.setFont(new Font("Consolas", Font.PLAIN, 14));
		logTitle.setBounds(21, 89, 151, 29);
		getContentPane().add(logTitle);
		logField = new JTextArea();
		logField.setFont(new Font("Candara", Font.PLAIN, 13));
		JScrollPane log = new JScrollPane();
		log.setBounds(10, 119, 265, 331);
		log.setViewportView(logField);
		getContentPane().add(log);


		
		start = new JButton("Start");
		start.addActionListener(controller);
		start.setFont(new Font("Impact", Font.BOLD, 15));
		start.setBounds(185, 49, 78, 29);
		getContentPane().add(start);
		
		start.setEnabled(false);
		
	}
	/**
	 * This changeStatus method takes the status of the game, 
	 * and set the availability of the start button based on status. 
	 * @param status
	 */
	public void changeStatus(String status) {
		switch(status) {
		case "waiting":
			start.setEnabled(false);
			break;
		case "ready":
			start.setEnabled(true);
			break;
		case "start":
			start.setEnabled(false);
			break;
			
		}
	}
	
}
