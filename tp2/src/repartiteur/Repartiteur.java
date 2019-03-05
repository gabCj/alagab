package repartiteur;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import shared.ServeurCalculInterface;

public class Repartiteur {
    private static final String DISTANT_SERVER = "127.0.0.1";
    String[] args;
    ServeurCalculInterface distantServerStub;

    public static void main(String[] args) {

		if (args.length > 0) {
			Repartiteur repartiteur = new Repartiteur(args);
		    repartiteur.run();
		} else {
            System.out.println("Enter a repartiteur command.");
        }
		
	}

	public Repartiteur(String[] args) {
		super();
        this.args = args;

		/*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/

		distantServerStub = loadServerStub(DISTANT_SERVER);	
	}

	private void run() {
        try {
            appelRMIDistant();
        } catch (ServerNotActiveException e) {
            System.out.println("Erreur: " + e.getMessage());
        }        
	}

	private ServeurCalculInterface loadServerStub(String hostname) {
		ServeurCalculInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServeurCalculInterface) registry.lookup("serveurCalcul");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}


	private void appelRMIDistant() throws ServerNotActiveException {
		try {
            String messageFromServer = distantServerStub.sayHi(args[0]);
            System.out.println(messageFromServer);
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
    }
}