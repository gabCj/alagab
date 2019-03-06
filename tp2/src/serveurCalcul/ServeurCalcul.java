package serveurCalcul;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import shared.ServeurCalculInterface;
import shared.Operation;

public class ServeurCalcul implements ServeurCalculInterface {
    private final String PRIME = "prime";
    private final String PELL = "pell";
    
    private int maxOps;
    private int m;

    public static void main(String[] args) {
        if (args.length >= 3) {
            try {
                int max = Integer.parseInt(args[1]);
                int m = Integer.parseInt(args[2]);
                if (m < 0 || m > 100) {
                    System.out.println("Third server argument must be between 0 and 100.");
                    return;
                }
                ServeurCalcul server = new ServeurCalcul(max, m);
		        server.run(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Second server argument must be a number.");
            }   
        } else {
            System.out.println("First server argument for server Id.");
            System.out.println("Second server argument for max number of operations.");
            System.out.println("Third server argument for m (0% to 100%).");
            System.out.println("Fourth server argument for distant IP addr (optional).");
        }
	}

	public ServeurCalcul(int maxOps, int m) {
        super();
        this.maxOps = maxOps;
        this.m = m;
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
        System.out.println("task received. Task size = " + operations.size());
        int sum = 0;

        for (Operation operation : operations) {
            if (operation.op.equals(PRIME))
                sum += Operations.prime(operation.x);
            if (operation.op.equals(PELL))
                sum += Operations.pell(operation.x);
            sum = sum % 5000;
        }

        Random rand = new Random();
        int random = rand.nextInt(100) + 1;
        if (random <= m) //Falsify result
            sum++;

        return sum;
    }
}