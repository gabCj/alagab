package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

public interface ServeurCalculInterface extends Remote {

	String sayHi(String message) throws RemoteException;

}