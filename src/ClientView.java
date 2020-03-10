import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClientView extends JFrame {
	JLabel text1;
	JLabel text2;
	public ClientView() {
		setTitle("Game Twenty-one");
		setSize(300, 300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(500, 300);
		text1 = new JLabel("Username:");
		text1.setFont(new Font("Arial", Font.BOLD, 15));
		text1.setBounds(21, 0, 151, 29);
		text2 = new JLabel("Waiting for a new game...");
		text2.setFont(new Font("Arial", Font.BOLD, 15));
		text2.setBounds(21, 31, 200, 29);
		getContentPane().setLayout(null);
		getContentPane().add(text1);
		getContentPane().add(text2);
	}
	
	public static void main(String[] args) {
		ClientView cv = new ClientView();
		cv.setVisible(true);
	}

}
