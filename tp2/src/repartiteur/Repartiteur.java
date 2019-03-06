package repartiteur;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import shared.ServeurCalculInterface;
import shared.Operation;

class Server {
    Server(ServeurCalculInterface stub) {
        this.stub = stub;
    }
    public ServeurCalculInterface stub;
    public int maxOps;
}

public class Repartiteur {
    private final String FILE_PATH = "./operations/";
    private final String LOCAL_SERVER = "127.0.0.1";
    private final String DISTANT_SERVER = "132.207.89.143";
    private final String PRIME = "prime";
    private final String PELL = "pell";
    private final int THRESHOLD = 50;
    private final boolean SECURE_MODE = true;

    private ArrayList<Server> servers;
    private ArrayList<Operation> operations;
    private String OperationsFileName;

    public static void main(String[] args) {

		if (args.length >= 1) {
			Repartiteur repartiteur = new Repartiteur(args[0]);
		    repartiteur.run(args);
		} else {
            System.out.println("Enter a file name for repartiteur.");
        }
		
	}

	public Repartiteur(String fileName) {
		super();
        this.OperationsFileName = fileName;
        servers = new ArrayList<Server>();
        operations = new ArrayList<Operation>();
		/*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/
	}

	private void run(String[] serverIds) {
        loadServerStubs(serverIds);
        obtainServersLimit();
        try {
            appelRMIDistant();
        } catch (ServerNotActiveException e) {
            System.out.println("Erreur: " + e.getMessage());
        }        
	}

	private void loadServerStubs(String[] serverIds) {
		try {
            Registry registry = LocateRegistry.getRegistry(LOCAL_SERVER);
            for (int i = 1; i < serverIds.length; i++) {
                servers.add(new Server((ServeurCalculInterface) registry.lookup(serverIds[i])));
            }
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
    }
    
    /*
    This method is used Repartiteur start with the ids given to obtain each of those server's max ops limit.
    */
    private void obtainServersLimit() {
        try {
            for (Server server : servers) {
                server.maxOps = server.stub.obtainServerMaxOps();
            }
        } catch(RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }    
    }

    /*
    This is the main method that uses stub and distributes operations to servers
    */
	private void appelRMIDistant() throws ServerNotActiveException {
        
        String filePath = FILE_PATH + OperationsFileName;
        File operationsFile = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(operationsFile));
            String line;
            String[] parsedOperation; 
            while ((line = br.readLine()) != null) {
                parsedOperation = line.split(" ");

                //check if file content is valid
                if (!checkOperationvalidity(parsedOperation)) {
                    System.out.println("The file contains invalid operations and/or structure.");
                    return;
                }
                int x = Integer.parseInt(parsedOperation[1]);
                
                operations.add(new Operation(parsedOperation[0], x));
            }
            if (SECURE_MODE)  
                sendOpsToServersSecured();
            else
                sendToServersNotSecured();
		} catch (NumberFormatException e) {
            System.out.println("The file contains invalid operations and/or structure.");
        } catch (FileNotFoundException e) {
            System.out.println("The file contains invalid operations and/or structure.");
        } catch (IOException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /*
    This method is used with each operation in file to check its validity
    */
    private boolean checkOperationvalidity(String[] parsedOperation) {
        if (parsedOperation.length != 2)
            return false;
        if (!(parsedOperation[0].equals(PRIME) || parsedOperation[0].equals(PELL)))
            return false;
        return true;
    }

    /*
    This method send distributes operations to servers for the secure mode
    */
    private void sendOpsToServersSecured() {
        int sum = 0;
        int operationsSent = 0;
        int nextServer = 0;
        ArrayList<Operation> task;

        while (operationsSent < operations.size() && servers.size() != 0) {
            task = new ArrayList<Operation>();
            int taskSize = calculateTaskSize(servers.get(nextServer).maxOps);

            if (taskSize > (operations.size() - operationsSent)) {
                taskSize = operations.size() - operationsSent;
            }

            for (int i = operationsSent; i < (operationsSent + taskSize); i++) {
                task.add(operations.get(i));
            }

            try {
                sum = (sum + servers.get(nextServer).stub.calculate(task)) % 5000;

                operationsSent += taskSize;
                nextServer = (nextServer + 1) % servers.size();
            } catch(RemoteException e) {
                System.out.println("Tried accessing a server that shut down. Redistributing.");
                servers.remove(nextServer);
                if (servers.size() == 0)
                    System.out.println("There are no more active servers. Aborting.");
                nextServer = nextServer % servers.size();
            }
            
        }

        System.out.println(sum);
    }

    /*
    This method send distributes operations to servers for the non secure mode
    */
    private void sendToServersNotSecured() {
        // A faire
    }

    /*
    This method calculates the size of the task to send to server based on threshold and qi
    */
    private int calculateTaskSize(int qi) {
        return ((THRESHOLD / 100) * (5*qi)) + qi;
    }

}