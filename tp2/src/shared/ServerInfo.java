package shared;

public class ServerInfo implements java.io.Serializable {
    public ServerInfo(String serverId, int port) {
        this.serverId = serverId;
        this.port = port;
    }
    public String serverId;
    public int port;
}