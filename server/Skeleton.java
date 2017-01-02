import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Classe que interpreta os pedidos de cada cliente, retornando os
 * respetivos resultados
 */
public class Skeleton extends Thread {
	private User user;
	private Socket cliSocket;
	private PrintWriter out;
	private BufferedReader in;
	private AuctionHouse aucHouse;
	private Thread notificator;

	Skeleton(AuctionHouse aucHouse, Socket cliSocket) throws IOException {
		this.aucHouse = aucHouse;
		this.cliSocket = cliSocket;
		in = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));
		out = new PrintWriter(cliSocket.getOutputStream(), true);
		user = null;
		notificator = null;
	}

	public void run() {
		String request = null;

		while((request = readLine()) != null) {
			String response = interpreteRequest(request);

			if (!response.isEmpty())
				out.println(response + "\n");
		}

		endConnection();
	}

	private String interpreteRequest(String request) {
		try {
			return runCommand(request);
		} catch (RequestFailedException e) {
			return "EXCEPTION\n" + e.getMessage();
		} catch (ArrayIndexOutOfBoundsException e) {
			return "EXCEPTION\n" + "Os argumentos não foram especificados";
		}
	}

	private String runCommand(String request) throws ArrayIndexOutOfBoundsException, RequestFailedException {
		String[] keywords = request.split(" ", 2);

		switch(keywords[0].toUpperCase()) {
			case "REGISTAR":
				userMustBeLogged(false);
				return signUp(keywords[1]);
			case "LOGIN":
				userMustBeLogged(false);
				return login(keywords[1]);
			case "INICIAR":
				userMustBeLogged(true);
				return startAuction(keywords[1]);
			case "TERMINAR":
				userMustBeLogged(true);
				return closeAuction(keywords[1]);
			case "LISTAR":
				userMustBeLogged(true);
				return listAuctions();
			case "LICITAR":
				userMustBeLogged(true);
				return bid(keywords[1]);
			case "CONFIRMAR":
				userMustBeLogged(true);
				return acknowledgeNotifications(keywords[1]);
			default:
				throw new RequestFailedException(keywords[0] + " não é um comando válido");
		}
	}

	private String signUp(String arguments) throws RequestFailedException {
		String[] parameters = arguments.split(" ");

		try {
			if (parameters.length > 2)
				throw new RequestFailedException("O username/password não podem ter espaços");

			aucHouse.signUp(parameters[0], parameters[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		} catch (UsernameExistsException e) {
			throw new RequestFailedException(e.getMessage());
		}

		return "OK";
	}

	private String login(String arguments) throws RequestFailedException {
		String[] parameters = arguments.split(" ");

		try {
			user = aucHouse.login(parameters[0], parameters[1]);
			user.setSession(cliSocket);

			notificator = new Notificator(user, out);
			notificator.start();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		} catch (IOException e) {
			throw new RequestFailedException("Não foi possível iniciar sessão");
		} catch (NoAuthorizationException e) {
			throw new RequestFailedException(e.getMessage());
		}

		return "OK";
	}

	private String startAuction(String description) throws RequestFailedException {
		int auctionID;

		try {
			auctionID = aucHouse.startAuction(user, description);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		}

		return "OK\n" + auctionID;
	}

	private String closeAuction(String argument) throws RequestFailedException {
		Bid bestBid;

		try {
			int auctionID = Integer.parseInt(argument);
			bestBid = aucHouse.closeAuction(user, auctionID);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		} catch (NoAuthorizationException | InvalidAuctionException e) {
			throw new RequestFailedException(e.getMessage());
		}

		if (bestBid.buyer() == null)
			return "OK\n" + "Ninguém licitou o item. O item não foi vendido";

		String message = "O item foi vendido a " + bestBid.buyer() + " por " + bestBid.value() + "!";

		return "OK\n" + message;
	}

	private String bid(String arguments) throws RequestFailedException {
		String[] parameters = arguments.split(" ");

		try {
			int auctionID = Integer.parseInt(parameters[0]);
			float price = Float.parseFloat(parameters[1]);

			aucHouse.bid(auctionID, user, price);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		} catch (InvalidAuctionException | InvalidAmountException e) {
			throw new RequestFailedException(e.getMessage());
		}

		return "OK";
	}

	private String listAuctions() throws RequestFailedException {
		Set<Auction> auctions = aucHouse.listAuctions();
		StringBuilder sb = new StringBuilder();

		for(Auction auc: auctions)
			sb.append("\n").append(auc.toString(user));

		return "OK" + sb.toString();
	}

	private String acknowledgeNotifications(String argument) throws RequestFailedException {
		try {
			int amount = Integer.parseInt(argument);
			user.acknowledge(amount);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new RequestFailedException("Os argumentos dados não são válidos");
		}

		return "";
	}

	private void userMustBeLogged(boolean status) throws RequestFailedException {
		if (status == true && user == null)
			throw new RequestFailedException("É necessário iniciar sessão para aceder aos leilões");

		if (status == false && user != null)
			throw new RequestFailedException("Já existe uma sessão iniciada");
	}

	private void endConnection() {
		if (notificator != null)
			notificator.interrupt();

		try {
			cliSocket.close();
		} catch (IOException e) {
			System.out.println("Couldn't close client socket... Client won't care");
		}
	}

	private String readLine() {
		String line = null;

		try {
			line = in.readLine();
		} catch(IOException e) {
			endConnection();
		}

		return line;
	}
}
