import java.io.Serializable;

public class Card implements Serializable{
	private String suit;
	private String num;
	private int point;
	public Card(String s, String n, int pt) {
		suit = s;
		num = n;
		point = pt;
	}
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
	public String toString() {
		return String.format("%2s {%s}", num, suit);
	}


}
