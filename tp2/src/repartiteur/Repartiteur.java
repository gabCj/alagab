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
import shared.RepertoireNomsInterface;
import shared.Operation;

class Server {
    Server(ServeurCalculInterface stub) {
        this.stub = stub;
    }
    public ServeurCalculInterface stub;
    public int maxOps;
}

public class Repartiteur {
    private final String REPARTITEUR_NOM = "repartiteur";
    private final String REPARTITEUR_MP = "123";
    private final String FILE_PATH = "./operations/";
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
		    repartiteur.run();
		} else {
            System.out.println("Enter an operation file name for repartiteur.");
        }	
	}

	public Repartiteur(String fileName) {
        this.OperationsFileName = fileName;
        servers = new ArrayList<Server>();
        operations = new ArrayList<Operation>();
	}

	private void run() {
        loadServerStubs();
        if (servers.size() != 0) {
            obtainServersLimit();
            distributeOperations(); 
        }      
	}

	private void loadServerStubs() {
		try {
            Registry registry = LocateRegistry.getRegistry(/*DISTANT_SERVER*/);
            RepertoireNomsInterface repertoireStub = (RepertoireNomsInterface) registry.lookup("RepertoireNoms");
            ArrayList<String> serverIds;

            //Ask RepertoireNoms for servers
            serverIds = repertoireStub.authenticateRepartiteur(REPARTITEUR_NOM, REPARTITEUR_MP);

            if (serverIds != null) {
                for (String serverId : serverIds) {
                    servers.add(new Server((ServeurCalculInterface) registry.lookup(serverId)));
                }
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
    This method is used by Repartiteur with the server ids to obtain each of those server's max ops limit.
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
	private void distributeOperations() {
        
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
        int sum = 0; //operation results sum
        int operationsSent = 0; //distribution progress
        int nextServer = 0; //server index for distribution
        ArrayList<Operation> task;
        int taskSize = 0;
        int numberOfRequest = 0; //How many server requests were made during distribution

        long start = System.nanoTime();
        while (operationsSent < operations.size()) {
            int taskResult = -1;

            //task result of -1 means the server refused the task
            while (taskResult == -1) {
                task = new ArrayList<Operation>();
                taskSize = calculateTaskSize(servers.get(nextServer).maxOps);
                
                //make sure there are enough operations left for taskSize
                if (taskSize > (operations.size() - operationsSent)) {
                    taskSize = operations.size() - operationsSent;
                }

                //fill task with the next taskSize operations
                for (int i = operationsSent; i < (operationsSent + taskSize); i++) {
                    task.add(operations.get(i));
                }

                //send to server
                try {
                    taskResult = servers.get(nextServer).stub.calculate(task, REPARTITEUR_NOM);
                    numberOfRequest++;
                }catch(RemoteException e) {
                    System.out.println("Tried accessing a server that shut down. Redistributing.");
                    servers.remove(nextServer);
                    if (servers.size() == 0) {
                        System.out.println("There are no more active servers. Aborting.");
                        return;
                    }        
                }

                // move to next server
                nextServer = (nextServer + 1) % servers.size();
            }
            
            //a server as calculated a result for current task
            sum = (sum + taskResult) % 5000;
            operationsSent += taskSize;
        }
        long end = System.nanoTime();

        //all done
        System.out.println("The result is " + sum + ". This was calculated in " + 
                            numberOfRequest + " server requests in " + (end - start) + " seconds.");
    }

    /*
    This method send distributes operations to servers for the non secure mode
    */
    private void sendToServersNotSecured() {
        int sum = 0; //operation results sum
        int operationsSent = 0; //distribution progress
        int nextServer1 = 0, nextServer2 = 0, nextServer3 = 0; //server indexes for distribution
        ArrayList<Operation> task;
        int taskSize = 0;
        int numberOfRequest = 0; //How many server requests were made during distribution

        long start = System.nanoTime();
        while (operationsSent < operations.size()) {
            int taskResult1 = -1;
            int taskResult2 = -1;
            int taskResult3 = -1;

            //task result of -1 means the server refused the task. All server must have a result before comparing in unsecured mode.
            while (taskResult1 == -1 || taskResult2 == -1 || taskResult3 == -1) {
                task = new ArrayList<Operation>();

                //get task size
                int taskSize1 = calculateTaskSize(servers.get(nextServer1).maxOps);
                nextServer2 = (nextServer1 + 1) % servers.size();
                int taskSize2 = calculateTaskSize(servers.get(nextServer2).maxOps);
                nextServer3 = (nextServer2 + 1) % servers.size();
                int taskSize3 = calculateTaskSize(servers.get(nextServer3).maxOps);
                
                taskSize = Math.min(taskSize1, Math.min(taskSize2, taskSize3)); //task must be the same for all 3 servers.
                
                //make sure there are enough operations left for taskSize
                if (taskSize > (operations.size() - operationsSent)) {
                    taskSize = operations.size() - operationsSent;
                }

                //fill task with the next taskSize operations
                for (int i = operationsSent; i < (operationsSent + taskSize); i++) {
                    task.add(operations.get(i));
                }

                //send to server
                try {
                    taskResult1 = servers.get(nextServer1).stub.calculate(task, REPARTITEUR_NOM);
                    numberOfRequest++;
                }catch(RemoteException e) {
                    System.out.println("Tried accessing a server that shut down. Redistributing.");
                    servers.remove(nextServer1);
                    if (servers.size() == 0) {
                        System.out.println("There are no more active servers. Aborting.");
                        return;
                    }        
                }
                try {
                    taskResult2 = servers.get(nextServer2).stub.calculate(task, REPARTITEUR_NOM);
                    numberOfRequest++;
                }catch(RemoteException e) {
                    System.out.println("Tried accessing a server that shut down. Redistributing.");
                    servers.remove(nextServer2);
                    if (servers.size() == 0) {
                        System.out.println("There are no more active servers. Aborting.");
                        return;
                    }        
                }
                try {
                    taskResult3 = servers.get(nextServer3).stub.calculate(task, REPARTITEUR_NOM);
                    numberOfRequest++;
                }catch(RemoteException e) {
                    System.out.println("Tried accessing a server that shut down. Redistributing.");
                    servers.remove(nextServer3);
                    if (servers.size() == 0) {
                        System.out.println("There are no more active servers. Aborting.");
                        return;
                    }        
                }

                nextServer1 = (nextServer3 + 1) % servers.size();
            }
            
            //servers have calculated a result for current task
            int taskResult = -1;
            if (taskResult1 == taskResult2)
                taskResult = taskResult1;
            else if (taskResult1 == taskResult3)
                taskResult = taskResult1;
            else if (taskResult2 == taskResult3)
                taskResult = taskResult2;

            if (taskResult != -1) {
                sum = (sum + taskResult) % 5000;
                operationsSent += taskSize;
            }      
            
        }
        long end = System.nanoTime();

        //all done
        System.out.println("The result is " + sum + ". This was calculated in " + 
                            numberOfRequest + " server requests in " + (end - start) + " seconds.");
    }

    /*
    This method calculates the size of the task to send to server based on threshold and qi
    */
    private int calculateTaskSize(int qi) {
        return (int) (((double)THRESHOLD / 100) * (5*qi)) + qi;
    }

}