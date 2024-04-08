import java.io.Serializable;
import java.net.InetSocketAddress;

public class Packet<T> implements Serializable
{
    public InetSocketAddress SOURCE_SOCKET_ADDRESS = new InetSocketAddress(Application.LOCAL_IP_ADDRESS, Application.INTERNAL_PORT);
    private T DATA;

    public Packet(T DATA)
    {
        this.DATA = DATA;
    }
    public T getDATA()
    {
        return this.DATA;
    }
    public void setDATA(T DATA)
    {
        this.DATA = DATA;
    }
    public String getSourceIP()
    {
        return this.SOURCE_SOCKET_ADDRESS.getHostName();
    }
    @Override
    public String toString()
    {
        return"PACKET<" + this.SOURCE_SOCKET_ADDRESS + "><C"+ this.DATA + ">";
    }
}