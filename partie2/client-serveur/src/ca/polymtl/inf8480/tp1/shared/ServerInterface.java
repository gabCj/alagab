package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {

	String openSession(String login, String password) throws RemoteException;

    ArrayList<ArrayList<String>> getGroupList(int checksum) throws RemoteException;

    String pushGroupList(ArrayList<ArrayList<String>> groupsDef) throws RemoteException;

    String lockGroupList() throws RemoteException;

    void sendMail(String subjet, String addrDest, String content) throws RemoteException;

    void listMails(boolean justUnread) throws RemoteException;

    void readMail(int id) throws RemoteException;

    void deleteMail(int id) throws RemoteException;

    void searchMail(ArrayList<String> keywords) throws RemoteException;
}
