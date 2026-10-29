package ClientPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientTP3 {

	public static void main(String[] args) {
		   String adresseServeur = "192.168.1.16";
		   int port = 1234;

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(InetAddress.getByName(adresseServeur), port));
			System.out.println("Connecté au serveur " + socket.getRemoteSocketAddress());

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String message = in.readLine();
			System.out.println("Message du serveur : " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}