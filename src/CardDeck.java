
import java.util.ArrayList;
import java.util.Collections;

/**
 * This is the class of a deck of cards.
 * It has an arrayList that contains 52 Card objects initially. 
 * It is able to shuffle, initialize and draw cards. 
 */
public class CardDeck {
	private ArrayList<Card> deck = new ArrayList<Card>();
	//Constructor
	public CardDeck(){
		init();
	}
	/**
	 * The init method initialize the whole deck.
	 */
	public void init() {
		deck.clear();
		for(int i = 2; i<=10; i++) {
			deck.add(new Card("Spade",i+"",i));
			deck.add(new Card("Heart",i+"",i));
			deck.add(new Card("Club",i+"",i));
			deck.add(new Card("Diamond",i+"",i));
		}
		deck.add(new Card("Spade","A",11));
		deck.add(new Card("Heart","A",11));
		deck.add(new Card("Club","A",11));
		deck.add(new Card("Diamond","A",11));
		
		deck.add(new Card("Spade","J",10));
		deck.add(new Card("Heart","J",10));
		deck.add(new Card("Club","J",10));
		deck.add(new Card("Diamond","J",10));
		
		deck.add(new Card("Spade","Q",10));
		deck.add(new Card("Heart","Q",10));
		deck.add(new Card("Club","Q",10));
		deck.add(new Card("Diamond","Q",10));
		
		deck.add(new Card("Spade","K",10));
		deck.add(new Card("Heart","K",10));
		deck.add(new Card("Club","K",10));
		deck.add(new Card("Diamond","K",10));
		
		shuffle();
	}
	//getter
	public ArrayList<Card> getDeck(){
		return deck;
	}
	/**
	 * The shuffle method shuffles the cards.
	 */
	public void shuffle() {
		Collections.shuffle(deck);
	}
	/**
	 * The draw method remove and return the first card at the top of the deck.
	 * The method is synchronized.
	 * @return A card.
	 */
	public Card draw() {
		synchronized(this) {
			return deck.remove(0);
		}
	}
}
