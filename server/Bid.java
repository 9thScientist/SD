/*
 * A Bid representa o desejo de um utilizador obter o item leiloado. O utilizador pode,
 * a qualquer altura, aumentar o valor da sua licitação.
 */
public class Bid {
	private final User buyer;
	private final float value;

	Bid(User buyer, float value) {
		this.buyer = buyer;
		this.value = value;
	}

	public User buyer() {
		return buyer;
	}

	public float value() {
		return value;
	}

	public Object clone() {
		return new Bid(buyer, value);
	}
}
