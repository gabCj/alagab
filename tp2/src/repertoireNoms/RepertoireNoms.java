package repertoireNoms;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import shared.RepertoireNomsInterface;

public class RepertoireNoms implements RepertoireNomsInterface {
    private final String REPARTITEUR_NOM = "repartiteur";
    private final String REPARTITEUR_MP = "123";

    private ArrayList<String> serverDomains;
    private boolean repartiteurAuthenticated;

    public static void main(String[] args) {
            RepertoireNoms repertoire = new RepertoireNoms(args);
            repertoire.run();    
    }
    
    public RepertoireNoms(String[] serverDomains) {
        super();
        this.serverDomains = new ArrayList<String>();
        repartiteurAuthenticated = false;
        for (int i = 1; i < serverDomains.length; i++) {
            this.serverDomains.add(serverDomains[i]);
        }
    }

    private void run() {
		try {
			RepertoireNomsInterface stub = (RepertoireNomsInterface) UnicastRemoteObject
                .exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
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
    public ArrayList<String> authenticateRepartiteur(String name, String password) throws RemoteException {
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