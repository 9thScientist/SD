import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;

public class BidBig {
	private static final int port = 5000;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket cli = new Socket(args[0], port);
		ClientInfo info = new ClientInfo();
		Reader reader = new Reader(cli, info);
		Stub stub = new Stub(cli, info);

		reader.start();
		stub.start();
	}
}
