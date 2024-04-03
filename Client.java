import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client
{
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 30000;
    private static final int NUM_THREADS = 4;

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting up client...");

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        //CREATE A THREAD THAT LISTENS FOR USER INPUT

        System.out.println("Connecting to:");
        System.out.println("IP: " + SERVER_IP);
        System.out.println("Port: " + SERVER_PORT);

        Socket socket = executor.submit(new TaskManager.ConnectClient(SERVER_IP,SERVER_PORT)).get();

        if(socket.isConnected())
        {
            System.out.println("Connection successful");
        }
        else
        {
            System.out.println("Connection unsuccessful");
        }
    }
}
