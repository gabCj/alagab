package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

public interface ServeurCalculInterface extends Remote {

    int pell(int x) throws RemoteException;

    int prime(int x) throws RemoteException;
}