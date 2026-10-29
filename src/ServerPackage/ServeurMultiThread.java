package ServerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ClientPackage.GestionClient;

public class ServeurMultiThread {

	public static void main(String[] args) {
		int port = 1234;
		int compteurClient = 0;
		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("Le serveur a démarré sur le port : " + port);
			System.out.println("En attente de connexions...");
			while (true) {
				Socket client = server.accept();
				compteurClient++;
				System.out.println("Client n°" + compteurClient + " connecté depuis " + client.getRemoteSocketAddress());
				new Thread(new GestionClient(client, compteurClient)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}