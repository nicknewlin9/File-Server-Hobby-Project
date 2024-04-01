import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server
{
    public static final int SERVER_PORT = 3000;

    public static void main(String[] args)
    {
        try
        {
            //OPEN LISTENING CHANNEL
            ServerSocketChannel listenChannel = ServerSocketChannel.open();
            listenChannel.bind(new InetSocketAddress(SERVER_PORT));

            while(true)
            {
                //CONNECT TO CLIENT //MAKE A NEW TASK THAT TAKES THE SOCKET CHANNEL AS ARGUMENT
                SocketChannel channel = listenChannel.accept();
                System.out.println("Client connected.");
                ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);

                int numBytesRead = channel.read(receiveBuffer);
                receiveBuffer.flip();
                byte[] messageArray = new byte[numBytesRead];
                receiveBuffer.get(messageArray);
                String message = new String(messageArray);
                System.out.println("Received the message: " + message);

                ByteBuffer sendBuffer = ByteBuffer.wrap(message.getBytes());
                channel.write(sendBuffer);
                channel.close();
                System.out.println("Client disconnected.");
            }
        }
        catch (IOException exception)
        {
            System.err.println("NETWORK ERROR");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static boolean List()
    {
        System.out.println();
        return true;
    }
    public static boolean Delete()
    {
        System.out.println();
        return true;
    }
    public static boolean Rename()
    {
        System.out.println();
        return true;
    }
    public static boolean Download()
    {
        System.out.println();
        return true;
    }
    public static boolean Upload()
    {
        System.out.println();
        return true;
    }
}