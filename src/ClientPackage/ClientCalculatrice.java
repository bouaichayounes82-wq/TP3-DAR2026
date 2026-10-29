package ClientPackage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import CommonPackage.Operation;

public class ClientCalculatrice {

    public static void main(String[] args) {
        String adresseServeur = "localhost"; // IP du serveur pour le test en réseau
        int port = 1235;

        try (Socket socket = new Socket(); Scanner clavier = new Scanner(System.in)) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(adresseServeur), port));
            System.out.println("Connecté au serveur " + socket.getRemoteSocketAddress());

            ObjectOutputStream sortie = new ObjectOutputStream(socket.getOutputStream());
            sortie.flush();
            ObjectInputStream entree = new ObjectInputStream(socket.getInputStream());

            while (true) {
                System.out.print("Premier nombre (ou q pour quitter) : ");
                String saisie = clavier.next();
                if (saisie.equalsIgnoreCase("q")) {
                    break;
                }
                double nombre1 = Double.parseDouble(saisie);
                System.out.print("Opérateur (+ - * /) : ");
                String operateur = clavier.next();
                System.out.print("Deuxième nombre : ");
                double nombre2 = Double.parseDouble(clavier.next());

                sortie.writeObject(new Operation(nombre1, operateur, nombre2));
                sortie.flush();

                double resultat = (Double) entree.readObject();
                System.out.println("Résultat reçu du serveur : " + resultat);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}