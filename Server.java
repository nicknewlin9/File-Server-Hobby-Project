import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 30000;
    private static final int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        System.out.println("Starting up server...");

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        //CREATE A THREAD THAT LISTENS FOR USER INPUT

        executor.submit(new TaskManager.OpenListenSocket(SERVER_PORT));

        System.out.println("Hosting on:");
        System.out.println("IP: " + SERVER_IP);
        System.out.println("Port: " + SERVER_PORT);
    }
}