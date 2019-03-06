package serveurCalcul;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import shared.ServeurCalculInterface;
import shared.Operation;

public class ServeurCalcul implements ServeurCalculInterface {
    private final String PRIME = "prime";
    private final String PELL = "pell";
    
    private String id;
    private int maxOps;
    private int currentOps;

    public static void main(String[] args) {
        if (args.length >= 2) {
            try {
                int max = Integer.parseInt(args[1]);
                ServeurCalcul server = new ServeurCalcul(max);
		        server.run(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Second server argument must be a number.");
            }   
        } else {
            System.out.println("First server argument for server Id.");
            System.out.println("Second server argument for max number of operations.");
            System.out.println("Third server argument for distant IP addr (optional).");
        }
	}

	public ServeurCalcul(int maxOps) {
        super();
        this.maxOps = maxOps;
        this.currentOps = 0;
	}

	private void run(String id) {

		/*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/

		try {
			ServeurCalculInterface stub = (ServeurCalculInterface) UnicastRemoteObject
                .exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(id, stub);
			System.out.println("ServeurCalcul " + id + " ready.");
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
    public int obtainServerMaxOps() throws RemoteException {
        return this.maxOps;
    }

    @Override
    public int pell(int x) throws RemoteException {
        return Operations.pell(x);
    }

    @Override
    public int prime(int x) throws RemoteException {
        return Operations.prime(x);
    }

    @Override
    public int calculate(ArrayList<Operation> operations) throws RemoteException {
        int sum = 0;

        for (Operation operation : operations) {
            if (operation.op.equals(PRIME))
                sum += Operations.prime(operation.x);
            if (operation.op.equals(PELL))
                sum += Operations.pell(operation.x);
            sum = sum % 5000;
        }

        return sum;
    }
}