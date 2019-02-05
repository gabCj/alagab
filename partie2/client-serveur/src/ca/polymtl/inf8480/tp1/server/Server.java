package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
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
	public void openSession(String login, String password) throws RemoteException {
	}

    @Override
	public void getGroupList(int checksum) throws RemoteException {
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
