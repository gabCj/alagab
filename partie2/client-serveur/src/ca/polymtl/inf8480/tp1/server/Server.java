package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Objects;
import java.io.*;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

class LogInfo {
    public String password;
    public Map<String,String> emails;

    LogInfo(String password)
    {
        this.password = password;
        this.emails = new HashMap();
    } 
} 

public class Server implements ServerInterface {

    private Map<String,LogInfo> users;
    private Map<String,ArrayList<String>> groups;
    private boolean groupListLocked = false;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
        super();
        users = new HashMap();
        users.put("Gabriel", new LogInfo("123"));
        users.put("Aladin", new LogInfo("123"));
        users.put("Rafael", new LogInfo("123"));
        users.put("Michel", new LogInfo("123"));
        users.put("Adel", new LogInfo("123"));
        groups = new HashMap();
        groups.put("group1", new ArrayList<String>(Arrays.asList("Gabriel", "Aladin", "Rafael")));
        groups.put("group2", new ArrayList<String>(Arrays.asList("Michel", "Adel")));
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
        LogInfo info = users.get(login);
        if (info != null) {
            if (info.password.equals(password)) {
                foundUser = true;
                return "Successful login!";
            } else {
                return "Wrong password.";
            }
        }
          
        if (!foundUser)
            return "Wrong email.";

        return "Something went wrong.";
	}

    @Override
	public Map<String,ArrayList<String>> getGroupList(int checksum) throws RemoteException {
        if (groups.hashCode() != checksum)
            return groups;
        else
            return null;
	}

    @Override
	public String pushGroupList(Map<String,ArrayList<String>> groupsDef) throws RemoteException {
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
	public String sendMail(String subjet, String addrDest, String content) throws RemoteException {
        String status = "There was an error.";
        ArrayList<String> group = groups.get(addrDest);
        LogInfo userInfo;
        if (group != null) {
            status = "Sent mail to group.";
            for (String user : group) {
                userInfo = users.get(user);
                if (userInfo != null) {
                    userInfo.emails.put(subjet,content);
                }
            }
        }
        else {
            userInfo = users.get(addrDest);
            if (userInfo != null) {
                status = "Sent mail to user.";
                userInfo.emails.put(subjet,content);
            }
        }

        return status;
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
