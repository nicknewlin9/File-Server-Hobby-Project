import java.io.Serializable;

public class Packet<T> implements Serializable
{
    private T packet;

    public Packet(T packet)
    {
        this.packet = packet;
    }

    public T getPacket()
    {
        return packet;
    }
    public void setPacket(T packet)
    {
        this.packet = packet;
    }
    public Command getCommandFromPacket() throws ClassCastException
    {
        if(packet instanceof Command)
        {
            return (Command) getPacket();
        }
        else
        {
            throw new ClassCastException("CANNOT PARSE COMMAND FROM PACKET");
        }
    }
    public Response getResponseFromPacket() throws ClassCastException
    {
        if(packet instanceof Response)
        {
            return (Response) getPacket();
        }
        else
        {
            throw new ClassCastException("CANNOT PARSE RESPONSE FROM PACKET");
        }
    }
}