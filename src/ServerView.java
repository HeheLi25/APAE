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
	public void addWait() {
		changeWait(getWait()+1);
	}
	public void writeLog(String s) {
		logField.append( s + "\n" );
	}

	public ServerView() {
		setTitle("Game Server");
		setSize(300,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(300,200);
		in = new JLabel("0 player(s) in game.");
		in.setFont(new Font("Arial", Font.BOLD, 15));
		in.setBounds(21, 0, 151, 29);
		wait = new JLabel("0 player(s) waiting.");
		wait.setFont(new Font("Arial", Font.BOLD, 15));
		wait.setBounds(21, 31, 151, 29);
		getContentPane().setLayout(null);
		getContentPane().add(in);
		getContentPane().add(wait);
		logField = new JTextArea();
		JScrollPane log = new JScrollPane();
		log.setBounds(21, 68, 232, 361);
		log.setViewportView(logField);
		getContentPane().add(log);


		
		start = new JButton("Start");
		start.setFont(new Font("Arial", Font.BOLD, 15));
		start.setBounds(175, 27, 78, 29);
		getContentPane().add(start);

	}
	
	public static void main(String[] args) {
		ServerView sv = new ServerView();
		sv.setVisible(true);
	}
}
