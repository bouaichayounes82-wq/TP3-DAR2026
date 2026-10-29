package ServerPackage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import CommonPackage.Operation;

public class ClientProcess implements Runnable {

    private final Socket socket;
    private final int numeroClient;
    private static int compteurGlobal = 0;

    private static synchronized int incrementerCompteur() {
        compteurGlobal++;
        return compteurGlobal;
    }

    public ClientProcess(Socket socket, int numeroClient) {
        this.socket = socket;
        this.numeroClient = numeroClient;
    }

    @Override
    public void run() {
        try (socket) {
            ObjectOutputStream sortie = new ObjectOutputStream(socket.getOutputStream());
            sortie.flush();
            ObjectInputStream entree = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Operation op = (Operation) entree.readObject();
                double resultat = calculer(op);
                int total = incrementerCompteur();
                System.out.println("Client n°" + numeroClient + " : " + op.getNombre1() + " "
                        + op.getOperateur() + " " + op.getNombre2() + " = " + resultat
                        + " | Compteur global : " + total);
                sortie.writeObject(resultat);
                sortie.flush();
            }
        } catch (EOFException e) {
            System.out.println("Client n°" + numeroClient + " déconnecté.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double calculer(Operation op) {
        switch (op.getOperateur()) {
            case "+":
                return op.getNombre1() + op.getNombre2();
            case "-":
                return op.getNombre1() - op.getNombre2();
            case "*":
                return op.getNombre1() * op.getNombre2();
            case "/":
                if (op.getNombre2() == 0) {
                    System.out.println("Division par zéro impossible");
                    return Double.NaN;
                }
                return op.getNombre1() / op.getNombre2();
            default:
                System.out.println("Opérateur invalide");
                return Double.NaN;
        }
    }
}