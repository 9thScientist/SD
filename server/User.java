import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;

/*
 * Cada utilizador é definido por um username e uma password, através dos
 * quais são autenticados. Um utilizador tem também um buffer único a si, a
 * partir do qual envia e recebe notificações
 */
public class User implements Comparable<User> {
	private final String username;
	private final String password;
	private Socket session;
	private NotificationBuffer buffer;

	User(String username, String password) {
		this.username = username;
		this.password = password;
		buffer = new NotificationBuffer();
	}

	public int compareTo(User usr) {
		return username.compareTo(usr.username);
	}

	public boolean authenticate(String password) {
		return this.password.equals(password);
	}

	public void setSession(Socket sock) throws IOException {
		if (session != null && !session.isClosed())
			session.close();

		session = sock;
	}

	public void notificate(String message) {
		buffer.write(message);
	}

	public String readNotification() throws InterruptedException {
		return buffer.read();
	}

	public void acknowledge(int amount) {
		buffer.acknowledge(amount);
	}

	public void resend() {
		buffer.reset();
	}

	public String toString() {
		return username;
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o == null || (this.getClass() != o.getClass()))
			return false;

		User usr = (User) o;
		return username.equals(usr.username);
	}
}
