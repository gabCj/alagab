package shared;

public class Operation implements java.io.Serializable {
    public Operation(String op, int x) {
        this.op = op;
        this.x = x;
    }
    public String op;
    public int x;
}