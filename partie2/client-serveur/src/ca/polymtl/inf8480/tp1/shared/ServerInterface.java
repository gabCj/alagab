package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Map;

public interface ServerInterface extends Remote {

	String openSession(String login, String password) throws RemoteException, ServerNotActiveException;

    Map<String,ArrayList<String>> getGroupList(String checksum) throws RemoteException, ServerNotActiveException;

    String pushGroupList(Map<String,ArrayList<String>> groupsDef) throws RemoteException, ServerNotActiveException;

    String lockGroupList() throws RemoteException, ServerNotActiveException;

    String sendMail(String subjet, String addrDest, String content) throws RemoteException, ServerNotActiveException;

    ArrayList<Mail> listMails(boolean justUnread) throws RemoteException, ServerNotActiveException;

    ArrayList<Mail> readMail(String id) throws RemoteException, ServerNotActiveException;

    String deleteMail(String id) throws RemoteException, ServerNotActiveException;

    ArrayList<Mail> searchMail(ArrayList<String> keywords) throws RemoteException, ServerNotActiveException;
}
