package repertoireNoms;

import java.io.BufferedReader;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import shared.RepertoireNomsInterface;
import shared.ServerInfo;

public class RepertoireNoms implements RepertoireNomsInterface {
    private final String SERVER_LIST_PATH = "serverList/servers.txt";
    private final String REPARTITEUR_NOM = "repartiteur";
    private final String REPARTITEUR_MP = "123";
    private final int PORT = 5000;

    private ArrayList<ServerInfo> serverDomains;
    private boolean repartiteurAuthenticated;

    public static void main(String[] args) {
        RepertoireNoms repertoire = new RepertoireNoms();
        repertoire.run();    
           
    }
    
    public RepertoireNoms() {
        super();
        this.serverDomains = new ArrayList<ServerInfo>();
        repartiteurAuthenticated = false;
    }

    private void obtainServerList() throws FileNotFoundException, IOException {
        File file = new File(SERVER_LIST_PATH);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String serverInfo;
        while ((serverInfo = br.readLine()) != null) {
            String[] parsedInfo = serverInfo.split(" ");
            if (parsedInfo.length >= 2) {
                serverDomains.add(new ServerInfo(parsedInfo[0], parsedInfo[1]));
            }
            
        }
        br.close();
    }

    private void run() {
		try {
            obtainServerList();

			RepertoireNomsInterface stub = (RepertoireNomsInterface) UnicastRemoteObject
                .exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry(PORT);
			registry.rebind("RepertoireNoms", stub);
			System.out.println("RepertoireNoms ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
    }

    /*
    Methodes accessibles par RMI
    */
    public ArrayList<ServerInfo> authenticateRepartiteur(String name, String password) throws RemoteException {
        if (name.equals(REPARTITEUR_NOM) && password.equals(REPARTITEUR_MP)) {
            repartiteurAuthenticated = true;
            return serverDomains;
        }
        return null;
    }

    public boolean verifyRepartiteur(String name) throws RemoteException {
        if (name.equals(REPARTITEUR_NOM))
            return repartiteurAuthenticated;
        return false;
    }
}