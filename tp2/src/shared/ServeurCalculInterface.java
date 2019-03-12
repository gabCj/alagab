package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface ServeurCalculInterface extends Remote {

    int obtainServerMaxOps() throws RemoteException;

    int calculate(ArrayList<Operation> operations, String nomRepartiteur) throws RemoteException;
}