package ca.polymtl.inf8480.tp1.shared;

public class Mail implements java.io.Serializable
{
    public Mail(String subject, String content, String dateReceived, String sentBy) {
        this.subject = subject;
        this.content = content;
        this.dateReceived = dateReceived;
        this.sentBy = sentBy;
        this.hasBeenRead = false;
    }

    public String asList() {
        String returnString = subject + ", " + dateReceived + ", " + sentBy + ", ";
        if (hasBeenRead)
            returnString += "Has been read\n";
        else
            returnString += "Has not been read\n";
        return returnString;
    }

    public String toString() {
        String returnString = subject + "\n" + dateReceived + "\n" + sentBy + "\n\n" + content + "\n\n";
        return returnString;
    }

    public String subject;
    public String content;
    public String dateReceived;
    public String sentBy;
    public boolean hasBeenRead;
}