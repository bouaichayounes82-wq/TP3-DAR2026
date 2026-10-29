package ClientPackage;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
	
public class GestionClient implements Runnable{
	private final Socket client;
	private final int numeroClient;
	public GestionClient(Socket client,int numeroClient) {
		this.client=client;
		this.numeroClient=numeroClient;
	}
	
	@Override
	public void run() {
	    try (client; PrintWriter sortie = new PrintWriter(client.getOutputStream(), true)) {
	        sortie.println("vous etes le client n° " + numeroClient);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
			
}
		

