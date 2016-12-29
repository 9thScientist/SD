import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Reader extends Thread {
	private ClientInfo client;
	private Socket cliSocket;
	private BufferedReader in;

	Reader(Socket cliSocket, ClientInfo client) throws IOException {
		this.client = client;
		this.cliSocket = cliSocket;
		in = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));
	}

	public void run() {
		String line, header, content;

		while((line = readLine()) != null) {
			header = line;

			if (header.equals("NOTIFICATION"))
				content = readContent();
			else {
				switch(client.getCommand()) {
					case 4: content = readAuctions();
							break;
					case 5: content = readNewAuction();
							break;
					case 7: content = readCloseAuction();
							break;
					default: content = readContent();
				}
			}

			giveMessage(header, content);
		}

		System.out.println("\nLigação terminada pelo servidor");
		System.exit(1);
	}

	private void giveMessage(String header, String content) {
		if (header.equals("EXCEPTION"))
			client.setReply(false, "> " + content);
		else if (header.equals("OK"))
			client.setReply(true, content);
		else
			client.addNotification(content);
	}

	private String readAuctions() {
		String line;
		StringBuilder sb = new StringBuilder();

		while((line = readLine()) != null) {
			if (line.isEmpty())
				break;

			sb.append(line).append("\n");
			sb.append(readLine()).append("\n");
			sb.append(readLine()).append("\n\n");
		}

		if (sb.length() == 0)
			return "> Neste momento não temos nenhum leilão a decorrer!\n";

		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private String readNewAuction() {
		String newAuction = readLine();
		readLine();
		return "> O seu leilão é indentificado pelo número " + newAuction + "!\n";
	}

	private String readCloseAuction() {
		String message = readLine();
		readLine();
		return "> " + message + "\n";
	}

	private String readContent() {
		StringBuilder sb = new StringBuilder();
		String line;

		while((line = readLine()) != null) {
			if (line.isEmpty())
				break;

			sb.append(line).append("\n");
		}

		return sb.toString();
	}

	private String readLine() {
		String line = null;

		try {
			line = in.readLine();
		} catch (IOException e) {
			System.out.println("Não foi possível ler novas mensagens");
		}

		return line;
	}
}
