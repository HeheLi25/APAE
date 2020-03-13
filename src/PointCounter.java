import java.util.ArrayList;

public class PointCounter {
	public static int countPoint(ArrayList<Card> cards) {
		int result = 0;
		for(Card c: cards) {
			result = result + c.getPoint();
		}
		return result;
	}

}
