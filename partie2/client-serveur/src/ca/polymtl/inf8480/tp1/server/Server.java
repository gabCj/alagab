package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.io.*;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

class LogInfo {
    public String email;
    public String password;

    LogInfo(String email, String password)
    {
        this.email = email;
        this.password = password;
    } 
} 

public class Server implements ServerInterface {

    private ArrayList<LogInfo> users;
    private ArrayList<ArrayList<String>> groups;
    private boolean groupListLocked = false;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
        super();
        users = new ArrayList<LogInfo>();
        users.add(new LogInfo("Gabriel","123"));
        users.add(new LogInfo("Aladin","123"));
        users.add(new LogInfo("Rafael","123"));
        users.add(new LogInfo("Michel","123"));
        users.add(new LogInfo("Adel","123"));
        groups = new ArrayList<ArrayList<String>>();
        groups.add(new ArrayList<String>(Arrays.asList(users.get(0).email, users.get(1).email, users.get(2).email)));
        groups.add(new ArrayList<String>(Arrays.asList(users.get(3).email, users.get(4).email)));
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
                .exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
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
	 * Méthodes accessible par RMI.
	 */
	@Override
	public String openSession(String login, String password) throws RemoteException {
        boolean foundUser = false;
        for (LogInfo info : users ) {
            if (info.email.equals(login)) {
                if (info.password.equals(password)) {
                    foundUser = true;
                    return "Successful login!";
                } else {
                    return "Wrong password.";
                }
            }
        }
        
        if (!foundUser)
            return "Wrong email.";

        return "Something went wrong.";
	}

    @Override
	public ArrayList<ArrayList<String>> getGroupList(int checksum) throws RemoteException {
        System.out.println(checksum);
        System.out.println(Objects.hash(groups));
        if (Objects.hash(groups) != checksum)
            return groups;
        else
            return null;
	}

    @Override
	public String pushGroupList(ArrayList<ArrayList<String>> groupsDef) throws RemoteException {
        if (groupListLocked) {
            groups = groupsDef;
            groupListLocked = false;
            return "Distant group list successfully updated.";
        } else {
            return "Group list needs to be locked for this.";
        }
	}

    @Override
	public String lockGroupList() throws RemoteException {
        if (!groupListLocked) {
            groupListLocked = true;
            return "Group list is now locked.";
        } else {
            return "Group list already locked by another user.";
        }
	}

    @Override
	public void sendMail(String subjet, String addrDest, String content) throws RemoteException {
	}

    @Override
	public void listMails(boolean justUnread) throws RemoteException {
	}

    @Override
	public void readMail(int id) throws RemoteException {
	}

    @Override
	public void deleteMail(int id) throws RemoteException {
	}

    @Override
	public void searchMail(ArrayList<String> keywords) throws RemoteException {
	}
}
