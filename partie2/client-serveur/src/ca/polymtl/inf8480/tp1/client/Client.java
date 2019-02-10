package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.Objects;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Mail;


public class Client {
    private final static String DISTANT_SERVER = "132.207.89.143";
    private final static String LOCAL_SERVER = "127.0.0.1";
    private final static boolean ONLY_UNREAD_MAIL = false;

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
    private Map<String,ArrayList<String>> groups;


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
        groups = new HashMap();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		distantServerStub = loadServerStub(LOCAL_SERVER);	
	}

	private void run() {

		if (distantServerStub != null) {
            try {
                appelRMIDistant();
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
			
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


	private void appelRMIDistant() throws ServerNotActiveException {
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

    private void login() throws RemoteException, ServerNotActiveException {
        if (command.length < 3) {
            System.out.println("Not enough arguments for the login command.");
            return;
        }          
        if (command.length > 3)
        {
            System.out.println("Too many arguments for the login command.");
            return;
        }
        String loginState = distantServerStub.openSession(command[1],command[2]);
        System.out.println(loginState); 
        if (loginState.equals("Successful login!")) {
            try {
                File file = new File("./loginToken.txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("Successful login!");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }

    private void getGroupList()  throws RemoteException, ServerNotActiveException {
        Map<String,ArrayList<String>> tempGroups = new HashMap();
        tempGroups = distantServerStub.getGroupList(groups.hashCode());
        if (tempGroups != null)
            this.groups = tempGroups;
       
        for (String group : this.groups.keySet()) {
            System.out.println(group);
        }
        
    }

    private void publish() throws RemoteException, ServerNotActiveException {
        System.out.println(distantServerStub.pushGroupList(groups));
    }

    private void lock() throws RemoteException, ServerNotActiveException {
        System.out.println(distantServerStub.lockGroupList());
    }

    private void send() throws RemoteException, ServerNotActiveException {
        if (command.length < 4) {
            System.out.println("Not enough arguments for the send mail command.");
            return;
        }          
        if (command.length > 4)
        {
            System.out.println("Too many arguments for the send mail command.");
            return;
        }

        System.out.println(distantServerStub.sendMail(command[1], command[2], command[3]));
    }

    private void list() throws RemoteException, ServerNotActiveException {
        ArrayList<Mail> mailsRequested = distantServerStub.listMails(ONLY_UNREAD_MAIL);
        if (mailsRequested == null) {
            System.out.println("You are not logged in.");
            return;
        }
            
        for (Mail mail : mailsRequested) {
            System.out.println(mail.asList());
        }
    }

    private void read() throws RemoteException, ServerNotActiveException {
        if (command.length < 2) {
            System.out.println("Not enough arguments for the read mail command.");
            return;
        }          
        if (command.length > 2)
        {
            System.out.println("Too many arguments for the read mail command.");
            return;
        }
        Mail requestedMail = distantServerStub.readMail(command[1]);
        if (requestedMail == null) {
            System.out.println("Could not find mail.");
            return;
        }
        System.out.println(requestedMail.toString());
    }

    private void delete() throws RemoteException, ServerNotActiveException {
        if (command.length < 2) {
            System.out.println("Not enough arguments for the delete mail command.");
            return;
        }          
        if (command.length > 2)
        {
            System.out.println("Too many arguments for the delete mail command.");
            return;
        }
        System.out.println(distantServerStub.deleteMail(command[1]));
    }

    private void search() throws RemoteException, ServerNotActiveException {
        if (command.length < 2) {
            System.out.println("You need at least one keyword.");
            return;
        } 

        ArrayList<String> keywords = new ArrayList<String>();
        for (int i = 1; i < command.length; i++) {
            keywords.add(command[i]);
        }

        ArrayList<Mail> mailsFound = distantServerStub.searchMail(keywords);
        if (mailsFound == null) {
            System.out.println("You are not logged in.");
        } else {
            for (Mail mail : mailsFound) {
                System.out.println(mail.asList());
            }
        }
    }

}
