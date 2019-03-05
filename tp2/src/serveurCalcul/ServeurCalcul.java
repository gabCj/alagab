package serveurCalcul;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

import shared.ServeurCalculInterface;

public class ServeurCalcul implements ServeurCalculInterface {
    public static void main(String[] args) {
		ServeurCalcul server = new ServeurCalcul();
		server.run();
	}

	public ServeurCalcul() {
        super();
	}

	private void run() {

		/*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/

		try {
			ServeurCalculInterface stub = (ServeurCalculInterface) UnicastRemoteObject
                .exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("serveurCalcul", stub);
			System.out.println("ServeurCalcul ready.");
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
    Methodes accessible par RMI
    */
    @Override
    public int pell(int x) throws RemoteException {
        return Operations.pell(x);
    }

    @Override
    public int prime(int x) throws RemoteException {
        return Operations.prime(x);
    }
}