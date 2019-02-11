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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.nio.file.Paths;
import java.util.Arrays;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Mail;


public class Client {
    //info app client
    private final static String GROUPS_DB_FILE_LOCATION = "./clientDB/groups.txt";
    private final static String USER_DB_FILE_LOCATION = "./clientDB/user.txt";
    private final static String DISTANT_SERVER = "132.207.89.143";
    private final static String LOCAL_SERVER = "127.0.0.1";
    private final static boolean ONLY_UNREAD_MAIL = false;

    //commandes client
    private final static String LOGIN = "login";
    private final static String GROUP_LIST = "get-group-list";
    private final static String PUBLISH = "publish-group-list";
    private final static String LOCK = "lock-group-list";
    private final static String SEND = "send";
    private final static String READ = "read";
    private final static String LIST = "list";
    private final static String DELETE = "delete";
    private final static String SEARCH = "search";
    private final static String JOIN = "join-group";
    private final static String CREATE = "create-group";

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
        loadGroups();
        try {
            appelRMIDistant();
        } catch (ServerNotActiveException e) {
            System.out.println("Erreur: " + e.getMessage());
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
                case JOIN :
                    join();
                    break;
                case CREATE :
                    create();
                    break;
                default :
                    System.out.println("Invalid command");
            }
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
    }
    

    /*
    Server call methods using RMI
    */
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
        String checksum = getMD5checksum();
        if (checksum == null) {
            System.out.println("There was an error calculating checksum");
            return;
        }
        tempGroups = distantServerStub.getGroupList(checksum);
        if (tempGroups != null) {
            this.groups = tempGroups;
            updateGroupsDB();
        }
                 
        System.out.println("Groups are :\n");
        String printString = "";    
        for (String group : this.groups.keySet()) {
            printString += group + " {";
            for (String user : this.groups.get(group)) {
                printString += " " + user;
            }
            printString += " }\n";
        }
        System.out.println(printString); 
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
        String content = command[3];
        for (int i = 4; i < command.length; i++) {
            content += " " + command[i];
        }          
        System.out.println(distantServerStub.sendMail(command[1], command[2], content));
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
        ArrayList<Mail> requestedMail = distantServerStub.readMail(command[1]);
        if (requestedMail == null) {
            System.out.println("You are not logged in.");
        } else if (requestedMail.isEmpty()) {
            System.out.println("Could not find mail.");
        } else {
            for (Mail mail : requestedMail) {
                System.out.println(mail.toString());
            }
        }
        
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

    /*
    client only methods
    */
    private void join() {
        if (command.length < 2) {
            System.out.println("You need more argument for the join group command.");
            return;
        }
        if (!groups.containsKey(command[1])) {
            System.out.println("This group does not exist.");
            return;
        }
        
        try {
            BufferedReader userDBreader = new BufferedReader(new FileReader(USER_DB_FILE_LOCATION));
            String userName;
            if ((userName = userDBreader.readLine()) != null) {
                if (groups.get(command[1]).contains(userName)) {
                    System.out.println("You are already in this group.");
                } else {
                    groups.get(command[1]).add(userName);
                    updateGroupsDB();
                    System.out.println("You have joined group " + command[1]);
                }
            }
            userDBreader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void create() {
        if (command.length < 2) {
            System.out.println("You need more argument for the create group command.");
            return;
        }
        if (groups.containsKey(command[1])) {
            System.out.println("There is already a group with that name.");
            return;
        }

        groups.put(command[1], new ArrayList<String>());
        for (int i = 2; i < command.length; i++) {
            groups.get(command[1]).add(command[i]);
        }
        updateGroupsDB();
        System.out.println("Group created!");
    }

    private void loadGroups() {
        try {
            BufferedReader groupDBreader = new BufferedReader(new FileReader(GROUPS_DB_FILE_LOCATION));
            String groupInfo;
            while ((groupInfo = groupDBreader.readLine()) != null) {
                String[] parsedInfo = groupInfo.split(" ");
                groups.put(parsedInfo[0], new ArrayList<String>());
                for (int i = 1; i < parsedInfo.length; i++) {
                    groups.get(parsedInfo[0]).add(parsedInfo[i]);
                }
            }
            groupDBreader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void updateGroupsDB() {
        try {
            PrintWriter pw = new PrintWriter(GROUPS_DB_FILE_LOCATION);
            for (String group : groups.keySet()) {
                pw.write(group + " ");
                for (String user : groups.get(group)) {
                    pw.write(user + " ");
                }
                pw.write("\n");
            }
            pw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    private String getMD5checksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(GROUPS_DB_FILE_LOCATION);
            DigestInputStream dis = new DigestInputStream(is, md);
            while (dis.read() > 0);
            byte[] checksum = md.digest();
            is.close();
            dis.close();
            return Arrays.toString(checksum);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
