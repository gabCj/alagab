package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Group;


public class Client {
    private final static String LOGIN = "login";
    private final static String GROUP_LIST = "get-group-list";
    private final static String PUBLISH = "publish-group-list";
    private final static String LOCK = "lock-group-list";
    private final static String SEND = "send";
    private final static String READ = "read";
    private final static String LIST = "list";
    private final static String DELETE = "delete";
    private final static String SEARCH = "search";

    private ServerInterface distantServerStub = null;
	private String[] command;
    private Group[] groups;


	public static void main(String[] args) {

		if (args.length > 0) {
			Client client = new Client(args);
		    client.run();
		} else {
            System.out.println("Enter a client command.");
        }
		
	}

	public Client(String[] command) {
		super();
        this.command = command;

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		distantServerStub = loadServerStub("127.0.0.1");	
	}

	private void run() {

		if (distantServerStub != null) {
			appelRMIDistant();
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
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


	private void appelRMIDistant() {
		try {
			switch(command[0]) {
                case LOGIN :
                    login();
                    break;
                case GROUP_LIST :
                    getGroupList();
                    break;
                case PUBLISH :
                    publish();
                    break;
                case LOCK :
                    lock();
                    break;
                case SEND :
                    send();
                    break;
                case READ :
                    read();
                    break;
                case LIST :
                    list();
                    break;
                case DELETE :
                    delete();
                    break;
                case SEARCH :
                    search();
                    break;
                default :
                    System.out.println("Invalid command");
            }
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

    private void login() throws RemoteException {
        if (command.length < 3) {
            System.out.println("Not enough arguments for the login command.");
            return;
        }          
        if (command.length > 3)
        {
            System.out.println("Too many arguments for the login command.");
            return;
        }
        System.out.println(distantServerStub.openSession(command[1],command[2])); 
    }

    private void getGroupList()  throws RemoteException {
        Group[] groups = null;
        groups = distantServerStub.getGroupList(Arrays.deepHashCode(groups));
        if (groups != null)
            this.groups = groups;
    }

    private void publish() throws RemoteException {
        
    }

    private void lock()  throws RemoteException {
        
    }

    private void send()  throws RemoteException {
        
    }

    private void read()  throws RemoteException {
        
    }

    private void list()  throws RemoteException {
        
    }

    private void delete()  throws RemoteException {
        
    }

    private void search()  throws RemoteException {
        
    }

}
