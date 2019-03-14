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
import shared.RepertoireNomsInterface;
import shared.Operation;

public class ServeurCalcul implements ServeurCalculInterface {
    private final String PRIME = "prime";
    private final String PELL = "pell";
    private final int REFUSAL_CODE = -1;
    
    private int maxOps;
    private int reliability;
    RepertoireNomsInterface repertoireStub;

    public static void main(String[] args) {
        if (args.length >= 3) {
            try {
                int max = Integer.parseInt(args[1]);
                if (max <= 0)
                {
                    System.out.println("Second server argument must be between higher than 0.");
                    return;
                }
                int reliability = Integer.parseInt(args[2]);
                if (reliability < 0 || reliability > 100) {
                    System.out.println("Third server argument must be between 0 and 100.");
                    return;
                }
                ServeurCalcul server = new ServeurCalcul(max, reliability);
		        server.run(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Second and third server argument must be a number.");
            }   
        } else {
            System.out.println("First server argument for server Id.");
            System.out.println("Second server argument for max number of operations.");
            System.out.println("Third server argument for server reliability (0% to 100%).");
            System.out.println("Fourth server argument for distant IP addr (optional).");
        }
	}

	public ServeurCalcul(int maxOps, int reliability) {
        super();
        this.maxOps = maxOps;
        this.reliability = reliability;
	}

	private void run(String id) {

		try {
            Registry registry = LocateRegistry.getRegistry();
            repertoireStub = (RepertoireNomsInterface) registry.lookup("RepertoireNoms");

			ServeurCalculInterface stub = (ServeurCalculInterface) UnicastRemoteObject
                .exportObject(this, 0);

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
    public int calculate(ArrayList<Operation> task, String nomRepartiteur) throws RemoteException {
        //validate repartiteur
        if (!repertoireStub.verifyRepartiteur(nomRepartiteur)) {
            System.out.println("task received from unverified repartiteur.");
            return REFUSAL_CODE;
        }

        System.out.println("task received. Task size = " + task.size());
        Random rand = new Random();
        if (task.size() > maxOps && rand.nextInt(100) + 1 <= calculateRefusalProbability(task.size())) {
            System.out.println("task was refused.");
            return REFUSAL_CODE;
        }

        int sum = 0;

        for (Operation operation : task) {
            if (operation.op.equals(PRIME))
                sum += Operations.prime(operation.x);
            if (operation.op.equals(PELL))
                sum += Operations.pell(operation.x);
            sum = sum % 5000;
        }

        //Check if unreliable servers should falsify result
        int random = rand.nextInt(100) + 1;
        if (random <= reliability) {
            sum += rand.nextInt(100);
            System.out.println("Oups! False result returned.");
        }
            
        return sum;
    }

    /*
    Formula of T
    */
    private double calculateRefusalProbability(int taskSize) {
        return ((double)(taskSize - maxOps) / (5*maxOps)) * 100;
    }

}