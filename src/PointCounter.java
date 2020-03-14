import java.util.ArrayList;
/**
 * A tool class that used to count the total point of a list of cards.
 */
public class PointCounter {
	/**
	 * This method takes an ArrayList of cards and return the total point of them.
	 * @param cards : An ArrayList of Card.
	 * @return : The number of total points.
	 */
	public static int countPoint(ArrayList<Card> cards) {
		int result = 0;
		for(Card c: cards) {
			result = result + c.getPoint();
		}
		return result;
	}

}
