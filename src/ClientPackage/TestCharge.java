package ClientPackage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import CommonPackage.Operation;

public class TestCharge {

    public static void main(String[] args) throws InterruptedException {
        int nbClients = 5;
        int nbOperations = 100;
        Thread[] clients = new Thread[nbClients];

        for (int i = 0; i < nbClients; i++) {
            clients[i] = new Thread(() -> {
                try (Socket socket = new Socket("localhost", 1235)) {
                    ObjectOutputStream sortie = new ObjectOutputStream(socket.getOutputStream());
                    sortie.flush();
                    ObjectInputStream entree = new ObjectInputStream(socket.getInputStream());
                    for (int j = 0; j < nbOperations; j++) {
                        sortie.writeObject(new Operation(j, "+", 1));
                        sortie.flush();
                        entree.readObject();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            clients[i].start();
        }
        for (Thread t : clients) {
            t.join();
        }
        System.out.println("Test terminé : " + (nbClients * nbOperations) + " opérations envoyées.");
    }
}