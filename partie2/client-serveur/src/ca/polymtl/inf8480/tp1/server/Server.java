package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Group;

class LogInfo {
    public String email;
    public String password;
    public boolean logged;

    LogInfo(String email, String password, boolean logged)
    {
        this.email = email;
        this.password = password;
        this.logged = logged;
    } 
} 

public class Server implements ServerInterface {

    private LogInfo[] loggedInUsers;
    private Group[] groups;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
        super();
        loggedInUsers = new LogInfo[5];
        loggedInUsers[0] = new LogInfo("Gabriel","123",false);
        loggedInUsers[1] = new LogInfo("Aladin","123",false);
        loggedInUsers[2] = new LogInfo("Rafael","123",false);
        loggedInUsers[3] = new LogInfo("Michel","123",false);
        loggedInUsers[4] = new LogInfo("Adel","123",false);
        groups = new Group[2];
        groups[0] = new Group(loggedInUsers[0].email, loggedInUsers[1].email, loggedInUsers[2].email);
        groups[1] = new Group(loggedInUsers[3].email, loggedInUsers[4].email);
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
        for (int i = 0; i < loggedInUsers.length; i++) {
            if (loggedInUsers[i].email.equals(login)) {
                foundUser = true;
                if (loggedInUsers[i].password.equals(password)) {
                    loggedInUsers[i].logged = true;
                    return "Successfully logged in.";
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
	public Group[] getGroupList(int checksum) throws RemoteException {
        if (Arrays.deepHashCode(groups) != checksum)
            return groups;
        else
            return null;
	}

    @Override
	public void pushGroupList(String[] groupsDef) throws RemoteException {
	}

    @Override
	public void lockGroupList() throws RemoteException {
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
	public void searchMail(String[] keywords) throws RemoteException {
	}
}
