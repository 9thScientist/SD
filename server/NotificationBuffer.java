import java.util.ArrayList;

/**
 * Buffer thread-safe para envio e receção de notificações. Um leitor bloqueia
 * quando o buffer está vazio. Um escritor nunca bloqueia, visto que o buffer
 * não tem restrições de tamanho, podendo crescer indefinidamente.
 */
public class NotificationBuffer {
	private ArrayList<String> notifications;
	private int index;

	NotificationBuffer() {
		notifications = new ArrayList<>();
		index = 0;
	}

	synchronized public void write(String message) {
		notifications.add(message);
		notifyAll();
	}

	synchronized public String read() throws InterruptedException {
		while(isEmpty())
			wait();

		String message = notifications.get(index);
		index += 1;

		return message;
	}

	synchronized public void reset() {
		index = 0;
	}

	synchronized public void acknowledge(int amount) {
		for (int i = 0; i < amount; i++)
			notifications.remove(0);

		index = 0;
	}

	synchronized public boolean isEmpty() {
		return notifications.size() == index;
	}
}
