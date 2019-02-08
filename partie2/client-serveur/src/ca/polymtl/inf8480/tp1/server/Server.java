package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.io.*;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Mail;

class LogInfo {
    public String currentHostName;
    public String password;
    public ArrayList<Mail> emails;

    LogInfo(String password)
    {
        this.currentHostName = "";
        this.password = password;
        this.emails = new ArrayList<Mail>();
    } 
} 

public class Server implements ServerInterface {

    private Map<String,LogInfo> users;
    private Map<String,ArrayList<String>> groups;
    private ArrayList<String> loggedInUsers;
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
        loggedInUsers = new ArrayList<String>();
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
	public String openSession(String login, String password) throws RemoteException, ServerNotActiveException {
        boolean foundUser = false;
        LogInfo info = users.get(login);
        if (info != null) {
            if (info.password.equals(password)) {
                foundUser = true;
                loggedInUsers.add(RemoteServer.getClientHost());
                info.currentHostName = RemoteServer.getClientHost();
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
	public Map<String,ArrayList<String>> getGroupList(int checksum) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return null;

        if (groups.hashCode() != checksum)
            return groups;
        return null;
	}

    @Override
	public String pushGroupList(Map<String,ArrayList<String>> groupsDef) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return "You are not logged in.";

        if (groupListLocked) {
            groups = groupsDef;
            groupListLocked = false;
            return "Distant group list successfully updated.";
        } else {
            return "Group list needs to be locked for this.";
        }
	}

    @Override
	public String lockGroupList() throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return "You are not logged in.";

        if (!groupListLocked) {
            groupListLocked = true;
            return "Group list is now locked.";
        } else {
            return "Group list already locked by another user.";
        }
	}

    @Override
	public String sendMail(String subjet, String addrDest, String content) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return "You are not logged in.";

        // status changed if addrDest exists
        String status = "Destination address does not exist.";
        ArrayList<String> group = groups.get(addrDest);
        LogInfo userInfo;
        if (group != null) {
            status = "Sent mail to group.";
            for (String user : group) {
                userInfo = users.get(user);
                if (userInfo != null) {
                    userInfo.emails.add(
                        new Mail(subjet, content, Calendar.getInstance().getTime().toString(), RemoteServer.getClientHost()));
                }
            }
        }
        else {
            userInfo = users.get(addrDest);
            if (userInfo != null) {
                status = "Sent mail to user.";
                userInfo.emails.add(
                    new Mail(subjet, content, Calendar.getInstance().getTime().toString(), RemoteServer.getClientHost()));
            }
        }

        return status;
	}

    @Override
	public ArrayList<Mail> listMails(boolean justUnread) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return null;

        ArrayList<Mail> mailsRequested = new ArrayList<Mail>();
        Iterator<Map.Entry<String, LogInfo>> it = users.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, LogInfo> pair = it.next();
            if (!pair.getValue().currentHostName.isEmpty() && pair.getValue().currentHostName.equals(RemoteServer.getClientHost())) {
                if (justUnread) {
                    for (Mail mail : pair.getValue().emails) {
                        if (!mail.hasBeenRead) {
                            mailsRequested.add(mail);
                        }
                    }
                } else {
                    mailsRequested = pair.getValue().emails;
                }
                break;
            }
        }

        return mailsRequested;
	}

    @Override
	public Mail readMail(String id) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return null;

        Iterator<Map.Entry<String, LogInfo>> it = users.entrySet().iterator();
        Mail requestedMail = null;

        while (it.hasNext()) {
            Map.Entry<String, LogInfo> pair = it.next();
            if (!pair.getValue().currentHostName.isEmpty() && pair.getValue().currentHostName.equals(RemoteServer.getClientHost())) {
                for (Mail mail : pair.getValue().emails) {
                    if (mail.subject.equals(id)) {
                        mail.hasBeenRead = true;
                        requestedMail = mail;
                    }
                }
            }
        }

        return requestedMail;
	}

    @Override
	public String deleteMail(String id) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return "You are not logged in.";
        
        //return message will change if mail is found
        String returnMessage = "Could not find mail.";
        Iterator<Map.Entry<String, LogInfo>> it = users.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, LogInfo> pair = it.next();
            if (!pair.getValue().currentHostName.isEmpty() && pair.getValue().currentHostName.equals(RemoteServer.getClientHost())) {
                for (Mail mail : pair.getValue().emails) {
                    if (mail.subject.equals(id)) {
                        pair.getValue().emails.remove(mail);
                        returnMessage = "Mail successfully deleted";
                    }
                }
            }
        }

        return returnMessage;
	}

    @Override
	public ArrayList<Mail> searchMail(ArrayList<String> keywords) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.contains(RemoteServer.getClientHost()))
            return null;
        ArrayList<Mail> foundMails = new ArrayList<Mail>();

        for (String keyword : keywords) {
            Iterator<Map.Entry<String, LogInfo>> it = users.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, LogInfo> pair = it.next();
                if (!pair.getValue().currentHostName.isEmpty() && pair.getValue().currentHostName.equals(RemoteServer.getClientHost())) {
                    for (Mail mail : pair.getValue().emails) {
                        if (mail.content.contains(keyword) && !foundMails.contains(mail)) {
                            foundMails.add(mail);
                        }
                    }
                }
            }
        }
        
        return foundMails;
    }
    
}
