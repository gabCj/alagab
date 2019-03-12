package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface RepertoireNomsInterface extends Remote {

    ArrayList<String> authenticateRepartiteur(String name, String password) throws RemoteException;

    boolean verifyRepartiteur(String name) throws RemoteException;
}