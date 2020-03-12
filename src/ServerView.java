import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.Font;

public class ServerView extends JFrame{
	private JTextArea logField;
	private JButton start;
	private JLabel in;
	private JLabel wait;
	private Server controller;
	private JLabel status;

	public JButton getStart() {
		return start;
	}
	
	public void changeIn(int i) {
		in.setText(i+" player(s) in game.");
	}
	public int getIn() {
		return Integer.parseInt(in.getText().split(" ")[0]+"");
	}
	public void changeWait(int i) {
		wait.setText(i+" player(s) waiting.");
	}
	public int getWait() {
		return Integer.parseInt(wait.getText().split(" ")[0]+"");
	}
	public void changeInAndWait(int in, int wait) {
		changeIn(in);
		changeWait(wait);
	}
	public void writeLog(String s) {
		logField.append( s + "\n" );
	}

	public ServerView(Server s) {
		controller = s;
		setTitle("Game Server");
		setSize(300,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(300,200);
		in = new JLabel("0 player(s) in game.");
//		in.setFont(new Font("Arial", Font.BOLD, 15));
		in.setBounds(21, 0, 151, 29);
		wait = new JLabel("0 player(s) waiting.");
//		wait.setFont(new Font("Arial", Font.BOLD, 15));
		wait.setBounds(21, 31, 151, 29);
		getContentPane().setLayout(null);
		getContentPane().add(in);
		getContentPane().add(wait);
		JLabel logTitle = new JLabel("Running log:");
//		logTitle.setFont(new Font("Arial", Font.BOLD, 12));
		logTitle.setBounds(21, 89, 151, 29);
		getContentPane().add(logTitle);
		logField = new JTextArea();
		JScrollPane log = new JScrollPane();
		log.setBounds(10, 119, 265, 331);
		log.setViewportView(logField);
		getContentPane().add(log);


		
		start = new JButton("Start");
		start.addActionListener(controller);
//		start.setFont(new Font("Arial", Font.BOLD, 15));
		start.setBounds(175, 27, 78, 29);
		getContentPane().add(start);
		
		start.setEnabled(false);
		
		status = new JLabel("Status: waiting for players");
		status.setBounds(21, 61, 151, 29);
		getContentPane().add(status);
	}
	public void changeStatus(String status) {
		switch(status) {
		case "waiting":
			start.setEnabled(false);
			this.status = new JLabel("Status: waiting for players");
			break;
		case "ready":
			start.setEnabled(true);
			this.status = new JLabel("Status: ready to start");
			break;
		case "start":
			start.setEnabled(false);
			this.status = new JLabel("Status: in game");
			break;
			
		}
	}
	
}
