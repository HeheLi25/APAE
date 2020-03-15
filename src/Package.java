import java.io.Serializable;
/**
 * A package is an Serializable object that can be transport between server and clients.
 * One package contains 2 things. One type String and one Serializable object. 
 */
public class Package implements Serializable{
	/**
	 * -----------Types of package-----------
	 * ---From Server to the Clients---
	 * MESSAGE: The package contains a single String to be displayed to the player.
	 * DEALER: The package contains a boolean, which means whether the player has been chosen to be the dealer.
	 * DEALER_CARD: The package contains a card object, but it is only used for choosing dealer, so the client should not store it.
	 * CARD: The package contains a Card object drawn from the deck.
	 * ASK: Inform the clients to choose whether to pass or draw card. Contains no object.
	 * CLEAN: Inform the clients to delete all the cards they have received. 
	 * END: Inform the clients that the round is end. Contains the number of stack change (Integer).
	 * EXPLODE: Inform the dealer when a player is out of the game. Contains the player's name.  
	 * ---From Clients to the Server---
	 * REGISTER: The package contains a String which is the name of the player.
	 * DRAW: After received ASK, the client choose to draw a new card. Server should send a CARD to the client.
	 * PASS: After received ASK, the client choose to pass the round. 
	 * OUT: Inform the server that the point is over 21, so the player lose and should go back to the waiting list.
	 * QUIT: Inform the server that the player quit the game.
	 */
	private String type;
	private Serializable object;
	
	public Package(String t, Serializable o) {
		type = t;
		object = o;
	}
	//getters
	public String getType() {
		return type;
	}
	public Serializable getObject() {
		return object;
	}
	

}
