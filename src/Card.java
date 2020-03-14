import java.io.Serializable;

/**
 * The class of one card.
 */
public class Card implements Serializable{
	/**
	 * suit: Spade, Heart, Club, Diamond
	 */
	private String suit;
	/**
	 * num: 2,3,4,5,6,7,8,9,10,J,Q,K,A
	 */
	private String num;
	/**
	 * point: 2,3,4,5,6,7,8,9,10,11
	 */
	private int point;
	//Constructor
	public Card(String s, String n, int pt) {
		suit = s;
		num = n;
		point = pt;
	}
	//getters and setters.
	public String getSuit() {
		return suit;
	}
	public void setSuit(String suit) {
		this.suit = suit;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	//The format to display a card.
	public String toString() {
		return String.format("%2s {%s}", num, suit);
	}


}
