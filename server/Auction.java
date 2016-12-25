import java.util.Set;
import java.util.TreeSet;

public class Auction implements Comparable<Auction>{
	private final int id;
	private final User seller;
	private final String description;
	private Bid bestBid;
	Set<User> buyers;

	Auction(int id, User seller, String description) {
		this.id = id;
		this.description = description;
		this.seller = seller;
		buyers = new TreeSet<>();
		bestBid = new Bid(null, 0);
	}

	public int id() {
		return id;
	}

	public int compareTo(Auction a) {
		return id - a.id;
	}

	synchronized public void bid(User seller, float price) throws InvalidAmountException {
		if (bestBid.value() > price)
			throw new InvalidAmountException("Já existe uma licitação com um valor superior");

		buyers.add(seller);
		bestBid = new Bid(seller, price);
	}

	synchronized public Bid close() {
		for(User usr: buyers) {
			String msg = toString() + "\n    Comprador: " + bestBid.buyer().toString();
			usr.notificate(msg);
		}

		return bestBid;
	}

	public User seller() {
		return seller;
	}

	synchronized public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("    ID: ").append(id).append("\n");
		sb.append("    Vendedor: ").append(seller).append("\n");
		sb.append("    Maior licitação: ").append(bestBid.value()).append("\n");
		sb.append("    Descrição: ").append(description);

		return sb.toString();
	}

	synchronized public String toString(User usr) {
		StringBuilder sb = new StringBuilder();

		sb.append("    ID: ").append(id).append("\n");
		sb.append(userStatus(usr));
		sb.append("Vendedor: ").append(seller);
		sb.append("\t\tMaior licitação: ").append(bestBid.value());
		sb.append("\n    Descrição: ").append(description);

		return sb.toString();
	}

	synchronized private String userStatus(User user) {
		if (user.equals(bestBid.buyer()) && user.equals(seller))
			return "* + ";
		else if (user.equals(bestBid.buyer()))
			return "  + ";
		else if (user.equals(seller))
			return "*   ";
		else
			return "    ";
	}

	synchronized public Object clone() {
		Auction copy = new Auction(id, seller, description);
		copy.bestBid = bestBid;
		buyers.forEach(v -> copy.buyers.add(v));

		return copy;
	}
}
