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
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.nio.file.Paths;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Mail;

class LogInfo {
    public String password;
    public ArrayList<Mail> emails;

    LogInfo(String password)
    {
        this.password = password;
        this.emails = new ArrayList<Mail>();
    } 
}

class ListLock {
    public boolean groupListLocked = false;
    public String lockedBy = ""; 
    ListLock() {}
}

public class Server implements ServerInterface {
    private final static String GROUPS_DB_FILE_LOCATION = "./serverDB/groups.txt";
    private final static String USERS_DB_FILE_LOCATION = "./serverDB/users.txt";
    private final static String MAIL_DB_FILE_LOCATION = "./serverDB/mails.txt";

    private Map<String,LogInfo> users;
    private Map<String,ArrayList<String>> groups;
    private Map<String,String> loggedInUsers;
    private ListLock listLockInfo;
    private boolean mailDBlocked = false;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
        super();
        users = new HashMap();
        groups = new HashMap();
        loggedInUsers = new HashMap();
        listLockInfo = new ListLock();
	}

	private void run() {
        loadUsers();
        loadGroups();
        loadMails();

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
    
    private void loadUsers() {
        try {
            BufferedReader userDBreader = new BufferedReader(new FileReader(USERS_DB_FILE_LOCATION));
            String userInfo;
            while ((userInfo = userDBreader.readLine()) != null) {
                String[] parsedInfo = userInfo.split(" ");
                if (parsedInfo.length > 1)
                    users.put(parsedInfo[0], new LogInfo(parsedInfo[1]));
            }
            userDBreader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
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

    private void loadMails() {
        try {
            BufferedReader mailDBreader = new BufferedReader(new FileReader(MAIL_DB_FILE_LOCATION));
            String mailInfo;
            while ((mailInfo = mailDBreader.readLine()) != null) {
                String[] parsedInfo = mailInfo.split(" ");
                if (parsedInfo.length >= 11 && users.containsKey(parsedInfo[0])) {
                    String content = parsedInfo[10];
                    String date = parsedInfo[2];
                    for (int i = 3; i <= 7; i++) {
                        date += " " + parsedInfo[i];
                    }
                    for (int i = 11; i < parsedInfo.length; i++) {
                        content += " " + parsedInfo[i];
                    }
                    Mail newMail = new Mail(parsedInfo[1],content,date,parsedInfo[8]);
                    if (parsedInfo[9].equals("true"))
                        newMail.hasBeenRead = true;
                    users.get(parsedInfo[0]).emails.add(newMail);
                }
            }
            mailDBreader.close();
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

    private void updateMailDB() {
        if (!mailDBlocked) {
            mailDBlocked = true;
            try {
                PrintWriter pw = new PrintWriter(MAIL_DB_FILE_LOCATION);
                for (String user : users.keySet()) {
                    for (Mail mail : users.get(user).emails) {
                        pw.write(user + " " + mail.subject + " " + mail.dateReceived + " " + mail.sentBy + " ");
                        if (mail.hasBeenRead)
                            pw.write("true ");
                        else
                            pw.write("false ");
                        pw.write(mail.content + "\n");
                    }
                }
                pw.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            mailDBlocked = false;
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
                loggedInUsers.put(RemoteServer.getClientHost(),login);
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
	public Map<String,ArrayList<String>> getGroupList(String checksum) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return null;
   
        if (!getMD5checksum().equals(checksum))
            return groups;
               
        return null;
	}

    @Override
	public String pushGroupList(Map<String,ArrayList<String>> groupsDef) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return "You are not logged in.";

        if (listLockInfo.groupListLocked && listLockInfo.lockedBy.equals(RemoteServer.getClientHost())) {
            groups = new HashMap();
            for (String group : groupsDef.keySet()) {
                groups.put(group,groupsDef.get(group));
            }
            updateGroupsDB();
            listLockInfo.groupListLocked = false;
            listLockInfo.lockedBy = "";
            return "Distant group list successfully updated.";
        } else if (listLockInfo.groupListLocked) {
            return "Group list is already locked by another user.";
        } else {
            return "Group list needs to be locked for this.";
        }
	}

    @Override
	public String lockGroupList() throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return "You are not logged in.";

        if (!listLockInfo.groupListLocked) {
            listLockInfo.groupListLocked = true;
            listLockInfo.lockedBy = RemoteServer.getClientHost();
            return "Group list is now locked.";
        } else {
            return "Group list already locked by another user.";
        }
	}

    @Override
	public String sendMail(String subjet, String addrDest, String content) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return "You are not logged in.";

        // status changed if addrDest exists
        String status = "Destination address does not exist.";

        //find hostname's addr
        String userAddr = loggedInUsers.get(RemoteServer.getClientHost());

        ArrayList<String> group = groups.get(addrDest);
        LogInfo userInfo;
        boolean changeInMails = false;

        if (group != null) {
            status = "Sent mail to group.";
            for (String user : group) {
                userInfo = users.get(user);
                if (userInfo != null) {
                    userInfo.emails.add(
                        new Mail(subjet, content, Calendar.getInstance().getTime().toString(), userAddr));
                    changeInMails = true;
                }
            }
        }
        else {
            userInfo = users.get(addrDest);
            if (userInfo != null) {
                status = "Sent mail to user.";
                userInfo.emails.add(
                    new Mail(subjet, content, Calendar.getInstance().getTime().toString(), userAddr));
                changeInMails = true;
            }
        }

        if (changeInMails)
            updateMailDB();
        return status;
	}

    @Override
	public ArrayList<Mail> listMails(boolean justUnread) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return null;
        
        ArrayList<Mail> mailsRequested = new ArrayList<Mail>();
        ArrayList<Mail> availableMails = users.get(loggedInUsers.get(RemoteServer.getClientHost())).emails;

        if (justUnread) {
            for (Mail mail : availableMails) {
                if (!mail.hasBeenRead) {
                    mailsRequested.add(mail);
                }
            }
        } else {
            mailsRequested = availableMails;
        }

        return mailsRequested;
	}

    @Override
	public ArrayList<Mail> readMail(String id) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return null;

        ArrayList<Mail> userMails = users.get(loggedInUsers.get(RemoteServer.getClientHost())).emails;
        ArrayList<Mail> requestedMail = new ArrayList<Mail>();
        boolean changeInMails = false;

        for (Mail mail : userMails) {
            if (mail.subject.equals(id)) {
                mail.hasBeenRead = true;
                changeInMails = true;
                requestedMail.add(mail);
            }
        }

        if (changeInMails)
            updateMailDB();
        return requestedMail;
	}

    @Override
	public String deleteMail(String id) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return "You are not logged in.";
        
        //return message will change if mail is found
        String returnMessage = "Could not find mail.";

        ArrayList<Mail> userMails = users.get(loggedInUsers.get(RemoteServer.getClientHost())).emails;
        for (Mail mail : userMails) {
            if (mail.subject.equals(id)) {
                userMails.remove(mail);
                updateMailDB();
                returnMessage = "Mail successfully deleted";
                break;
            }
        }

        return returnMessage;
	}

    @Override
	public ArrayList<Mail> searchMail(ArrayList<String> keywords) throws RemoteException, ServerNotActiveException {
        if (!loggedInUsers.containsKey(RemoteServer.getClientHost()))
            return null;

        ArrayList<Mail> foundMails = new ArrayList<Mail>();
        ArrayList<Mail> userMails = users.get(loggedInUsers.get(RemoteServer.getClientHost())).emails;

        for (String keyword : keywords) {
            for (Mail mail : userMails) {
                if (mail.content.contains(keyword) && !foundMails.contains(mail)) {
                    foundMails.add(mail);
                }
            }
        }
   
        return foundMails;
    }
    
}
