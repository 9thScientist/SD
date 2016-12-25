import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionHouse {
	private Lock userLock;
	private Lock auctionLock;
	private Map<String, User> users;
	private Map<Integer, Auction> auctions;
	private int nextAuction;

	AuctionHouse() {
		users = new TreeMap<>();
		auctions = new TreeMap<>();
		userLock = new ReentrantLock();
		auctionLock = new ReentrantLock();
		nextAuction = 0;
	}

	public void signUp(String username, String password) throws UsernameExistsException {
		userLock.lock();
		try {
			if (users.containsKey(username))
				throw new UsernameExistsException("O nome de utilizador não está disponível");

			users.put(username, new User(username, password));
		} finally {
			userLock.unlock();
		}
	}

	public User login(String username, String password) throws NoAuthorizationException {
		User user;

		userLock.lock();
		try {
			user = users.get(username);
			if (user == null || !user.authenticate(password))
				throw new NoAuthorizationException("Os dados inseridos estão incorretos");
			user.resend();
		} finally {
			userLock.unlock();
		}

		return user;
	}

	public int startAuction(User user, String description) {
		int auctionID;

		auctionLock.lock();
		try {
			auctionID = nextAuction;
			nextAuction += 1;

			Auction auction = new Auction(auctionID, user, description);
			auctions.put(auctionID, auction);
		} finally {
			auctionLock.unlock();
		}

		return auctionID;
	}

	public Bid closeAuction(User user, int auctionID) throws InvalidAuctionException, NoAuthorizationException {
		Auction auction;

		auctionLock.lock();
		try {
			auction = getAuction(auctionID);

			if (!auction.seller().equals(user))
				throw new NoAuthorizationException("Apenas o vendedor pode terminar o leilão");

			auctions.remove(auctionID);
		} finally {
			auctionLock.unlock();
		}

		return auction.close();
	}

	public Set<Auction> listAuctions() {
		Set<Auction> auctionSet = new TreeSet<>();

		auctionLock.lock();
		try {
			auctions.forEach((k,auc) -> auctionSet.add((Auction) auc.clone()));
		} finally {
			auctionLock.unlock();
		}

		return auctionSet;
	}

	public void bid(int auctionID, User user, float price) throws InvalidAmountException, InvalidAuctionException {
		Auction auc = getAuction(auctionID);
		auc.bid(user, price);
	}

	private Auction getAuction(int auctionID) throws InvalidAuctionException {
		Auction auction;

		auctionLock.lock();
		try {
			auction = auctions.get(auctionID);

			if (auction == null)
				throw new InvalidAuctionException("O leilão indicado não existe");
		} finally {
			auctionLock.unlock();
		}

		return auction;
	}
}
