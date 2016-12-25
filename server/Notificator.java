import java.io.PrintWriter;

/*
 * Thread que envia para o cliente, de forma assíncrona,
 * as respetivas notificações.
 */
public class Notificator extends Thread {
	private final PrintWriter out;
	private final User user;

	Notificator(User user, PrintWriter out) {
		this.user = user;
		this.out = out;
	}

	public void run() {
		while(true) {
			try {
				String message = user.readNotification();
				out.println("NOTIFICATION\n" + message + "\n");
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
