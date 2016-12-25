import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Classe principal do servidor que inicializa os dados e espera por ligações
 * dos clientes
 */
public class Server {
	private static final int port = 5000;

	public static void main(String[] args) throws IOException {
		ServerSocket srv = new ServerSocket(port);
		AuctionHouse aucHouse = new AuctionHouse();

		while(true) {
			Socket cliSocket = srv.accept();
			Skeleton cli = new Skeleton(aucHouse, cliSocket);
			cli.start();
		}
	}
}
