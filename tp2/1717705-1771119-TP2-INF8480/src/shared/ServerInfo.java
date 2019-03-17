package shared;

public class ServerInfo implements java.io.Serializable {
    public ServerInfo(String serverId, String hostname) {
        this.serverId = serverId;
        this.hostname = hostname;
    }
    public String serverId;
    public String hostname;
}