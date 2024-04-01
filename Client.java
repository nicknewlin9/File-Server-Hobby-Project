import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client
{
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 3000;
    public static Scanner scanner = new Scanner(System.in);
    public static String currentCommand = "STARTUP";

    public static void main(String[] args)
    {
        try
        {
            InetAddress IPAddress = InetAddress.getByName(SERVER_IP);
            do
            {
                //CONSTRUCT COMMAND TO SEND TO SERVER (STRING)
                constructCommandFromUser();

                //CONNECT TO SERVER
                SocketChannel channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(IPAddress, SERVER_PORT));

                //SENDS COMMAND TO SERVER
                ByteBuffer sendBuffer = ByteBuffer.wrap(currentCommand.getBytes());
                channel.write(sendBuffer);

                //RECEIVE CONFIRMATION
                ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
                int numBytesRead = channel.read(receiveBuffer);
                receiveBuffer.flip();
                byte[] receiveBufferArray = new byte[numBytesRead];
                receiveBuffer.get(receiveBufferArray);
                System.out.println("Server received message: " + new String(receiveBufferArray));

                //CLOSE CLIENT CHANNEL
                channel.close();
            }
            while(!currentCommand.equals("QUIT"));
        }
        catch(UnknownHostException exception)
        {
            System.err.println("INVALID SERVER IP");
            exception.printStackTrace();
            System.exit(0);
        }
        catch(IOException exception)
        {
            System.err.println("CAN'T CONNECT TO SERVER");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void constructCommandFromUser()
    {
        String command = "";
        do
        {
            System.out.println("\nEnter a command: ");
            command = scanner.nextLine().toUpperCase();
            switch(command)
            {
                case "LIST":
                    System.out.println("\nTyped the List command.");
                    currentCommand = "LIST";
                    break;

                case "DELETE":
                    System.out.println("\nTyped the Delete command.");
                    System.out.println("Type name of file to delete: ");
                    String delFilename = scanner.nextLine();
                    currentCommand = "DELETE" + delFilename;
                    break;

                case "RENAME":
                    System.out.println("\nTyped the Rename command.");
                    System.out.println("Type name of file to rename: ");
                    String oldFilename = scanner.nextLine();
                    System.out.println("Type new file name: ");
                    String newFilename = scanner.nextLine();
                    currentCommand = "RENAME" + oldFilename + newFilename;
                    break;

                case "DOWNLOAD":
                    System.out.println("\nTyped the Download command.");
                    System.out.println("Type name of file to download: ");
                    String dnlFilename = scanner.nextLine();
                    currentCommand = "DOWNLOAD" + dnlFilename;
                    break;

                case "UPLOAD":
                    System.out.println("\nTyped the Upload command.");
                    System.out.println("Type name of file to upload: ");
                    String uplFilename = scanner.nextLine();
                    currentCommand = "UPLOAD" + uplFilename;
                    break;

                case "QUIT":
                    System.out.println("\nTyped the Quit command.");
                    System.out.println("\nShutting down...");
                    currentCommand = "QUIT";
                    break;

                default:
                    System.out.println("\nINVALID COMMAND\nVALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" OR \"QUIT\"");
                    currentCommand = "INVALID";
                    break;
            }
        }
        while(currentCommand.equals("INVALID"));
    }
}
