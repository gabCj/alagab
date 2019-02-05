package ca.polymtl.inf8480.tp1.client;

public class FakeServer {
	void openSession(String login, String password) throws RemoteException {
	}

    void getGroupList(int checksum) throws RemoteException {
	}

    void pushGroupList(String[] groupsDef) throws RemoteException {
	}

    void lockGroupList() throws RemoteException {
	}

    void sendMail(String subjet, String addrDest, String content) throws RemoteException {
	}

    void listMails(boolean justUnread) throws RemoteException {
	}

    void readMail(int id) throws RemoteException {
	}

    void deleteMail(int id) throws RemoteException {
	}

    void searchMail(String[] keywords) throws RemoteException {
	}
}
