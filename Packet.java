import java.io.Serializable;

public class Packet<T> implements Serializable
{
    private String sourceIP;
    private int destinationPort;
    private T data;

    public Packet(String sourceIP, int destinationPort, T data)
    {
        this.sourceIP = sourceIP;
        this.destinationPort = destinationPort;
        this.data = data;
    }
    public String getSourceIP()
    {
        return sourceIP;
    }
    public void setSourceIP(String sourceIP)
    {
        this.sourceIP = sourceIP;
    }
    public int getDestinationPort()
    {
        return destinationPort;
    }
    public void setDestinationPort(int destinationPort)
    {
        this.destinationPort = destinationPort;
    }
    public T getData()
    {
        return data;
    }
    public void setData(T data)
    {
        this.data = data;
    }
    public Command parseCommandFromPacket()
    {
        if(data instanceof Command)
        {
            return (Command) getData();
        }
        else
        {
            throw new ClassCastException("CANNOT PARSE COMMAND FROM PACKET");
        }
    }
    public Response parseResponseFromPacket()
    {
        if(data instanceof Response)
        {
            return (Response) getData();
        }
        else
        {
            throw new ClassCastException("CANNOT PARSE RESPONSE FROM PACKET");
        }
    }
    @Override
    public String toString()
    {
        return"SOURCE_IP: " + sourceIP + "\nDESTINATION_PORT: " + destinationPort + "\nCONTENTS: " + data;
    }
}