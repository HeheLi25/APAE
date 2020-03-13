import java.io.Serializable;

public class Package implements Serializable{
//	private String sender;
	private String type;
	/* MESSAGE
	 * START
	 * CARD
	 * COUNT
	 * END
	 * 
	 * REGISTER
	 * DRAW
	 */
	private Serializable object;
	
	public Package(String t, Serializable o) {
		type = t;
		object = o;
	}

	public String getType() {
		return type;
	}

	public Serializable getObject() {
		return object;
	}
	

}
