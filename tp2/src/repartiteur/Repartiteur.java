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

import shared.ServeurCalculInterface;

public class Repartiteur {
    private final String FILE_PATH = "./operations/";
    private final String LOCAL_SERVER = "127.0.0.1";
    private final String DISTANT_SERVER = "132.207.89.143";
    private final String PRIME = "prime";
    private final String PELL = "pell";

    String fileName;
    ServeurCalculInterface distantServerStub;

    public static void main(String[] args) {

		if (args.length == 1) {
			Repartiteur repartiteur = new Repartiteur(args[0]);
		    repartiteur.run();
		} else {
            System.out.println("Enter a file name for repartiteur.");
        }
		
	}

	public Repartiteur(String fileName) {
		super();
        this.fileName = fileName;

		/*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/

		distantServerStub = loadServerStub(LOCAL_SERVER);	
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
        String filePath = FILE_PATH + fileName;
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
                
                //send ServeurCalcul the operation
                if (parsedOperation[0].equals(PRIME))
                    System.out.println(distantServerStub.prime(x));
                else if (parsedOperation[0].equals(PELL))
                    System.out.println(distantServerStub.pell(x));
            }  
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (NumberFormatException e) {
            System.out.println("The file contains invalid operations and/or structure.");
        } catch (FileNotFoundException e) {
            System.out.println("The file contains invalid operations and/or structure.");
        } catch (IOException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    private boolean checkOperationvalidity(String[] parsedOperation) {
        if (parsedOperation.length != 2)
            return false;
        if (!(parsedOperation[0].equals(PRIME) || parsedOperation[0].equals(PELL)))
            return false;
        return true;
    }
}