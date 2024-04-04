import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskManager
{
    public static ExecutorService executor;
    public static Command currentCommand;
    public TaskManager(ExecutorService executor, Command currentCommand)
    {
        TaskManager.executor = executor;
        TaskManager.currentCommand = currentCommand;
    }
    public void setExecutor(ExecutorService executor)
    {
        TaskManager.executor = executor;
    }
    public ExecutorService getExecutor()
    {
        return TaskManager.executor;
    }
    public void setCurrentCommand(Command currentCommand)
    {
        TaskManager.currentCommand = currentCommand;
    }
    public Command getCurrentCommand()
    {
        return TaskManager.currentCommand;
    }

    public static class OpenServerSocket implements Callable<ServerSocket>
    {
        private final int SERVER_PORT;
        public OpenServerSocket(int port)
        {
            this.SERVER_PORT = port;
        }
        public ServerSocket call()
        {
            try
            {
                ServerSocket listenSocket = new ServerSocket(SERVER_PORT);
                System.out.println("HOSTING AT IP: | " + Server.getServerIp() + " | ON PORT: | " + SERVER_PORT + " |");
                return listenSocket;
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "OpenServerSocket");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class ListenForConnections implements Runnable
    {
        private final ServerSocket serverSocket;
        public ListenForConnections(ServerSocket serverSocket)
        {
            this.serverSocket = serverSocket;
        }
        public void run()
        {
            try
            {
                while(!(currentCommand.equals("QUIT CLIENT") || currentCommand.equals("QUIT SERVER") || currentCommand.equals("FORCE QUIT")))
                {
                    Socket socket = serverSocket.accept();
                    System.out.println("A CLIENT HAS CONNECTED.");
                    executor.submit(new ServeClientRequest(socket));
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "ListenForConnections");
                exception.printStackTrace();
            }
        }
    }

    public static class ConnectToHost implements Callable<Socket>
    {
        private final String SERVER_IP;
        private final int SERVER_PORT;
        public ConnectToHost(String SERVER_IP, int SERVER_PORT)
        {
            this.SERVER_IP = SERVER_IP;
            this.SERVER_PORT = SERVER_PORT;
        }
        public Socket call()
        {
            try
            {
                Socket socket = new Socket();
                InetAddress IPAddress = InetAddress.getByName(SERVER_IP);
                InetSocketAddress socketAddress = new InetSocketAddress(IPAddress, SERVER_PORT);
                socket.connect(socketAddress);
                System.out.println("CONNECTED TO IP: | " + SERVER_IP + " | ON PORT: | " + SERVER_PORT + " |");
                return socket;
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "ConnectToHost");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class ServeClientRequest implements Runnable
    {
        private final Socket socket;
        public ServeClientRequest(Socket socket)
        {
            this.socket = socket;
        }
        public void run()
        {
            try
            {
                Packet<?> receivedPacket = executor.submit(new ReceivePacket(socket)).get();
                Command receivedCommand = receivedPacket.getCommandFromPacket();
                Response response = executor.submit(new ProcessCommandFromClient(receivedCommand)).get();
                Packet<Response> responsePacket = new Packet<>(response);

                Socket clientConnection = TaskManager.executor.submit(new TaskManager.ConnectToHost(Client.getClientIp(),Client.getClientPort())).get();
                executor.submit(new SendPacket(clientConnection, responsePacket));
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "ServeClientRequest");
                exception.printStackTrace();
            }
        }
    }

    public static class SubmitRequest implements Callable<Response>
    {
        private final Socket socket;
        private final Command command;
        public SubmitRequest(Socket socket, Command command)
        {
            this.socket = socket;
            this.command = command;
        }
        public Response call()
        {
            try
            {
                Packet<Command> commandPacket = new Packet<>(command);
                executor.submit(new SendPacket(socket,commandPacket));
                Packet<?> receivedPacket = executor.submit(new ReceivePacket(socket)).get();
                return receivedPacket.getResponseFromPacket();
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "SubmitRequest");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class SendPacket implements Runnable
    {
        private final Socket socket;
        private final Packet<?> packet;
        public SendPacket(Socket socket, Packet<?> packet)
        {
            this.socket = socket;
            this.packet = packet;
        }
        public void run()
        {
            try
            {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(packet);
                System.out.println("SENT PACKET: " + packet);
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "SendPacket");
                exception.printStackTrace();
            }
        }
    }

    public static class ReceivePacket implements Callable<Packet<?>>
    {
        private final Socket socket;
        public ReceivePacket(Socket socket)
        {
            this.socket = socket;
        }
        public Packet<?> call()
        {
            try
            {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Packet<?> receivedPacket = (Packet<?>) objectInputStream.readObject();
                System.out.println("RECEIVED PACKET: " + receivedPacket);
                return receivedPacket;
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "ReceivePacket");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class List implements Callable<Response>
    {
        public Response call()
        {
            try
            {
                return new Response(true,"SERVER RECEIVED LIST COMMAND");
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "List");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class Delete implements Callable<Response>
    {
        private final String filenameToDelete;
        public Delete(String filename)
        {
            this.filenameToDelete = filename;
        }
        public Response call()
        {
            try
            {
                return new Response(true,"SERVER RECEIVED DELETE COMMAND");
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "Delete");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class Rename implements Callable<Response>
    {
        private final String filenameToRename;
        private final String newFilename;
        public Rename(String filenameToRename, String newFilename)
        {
            this.filenameToRename = filenameToRename;
            this.newFilename = newFilename;
        }
        public Response call()
        {
            try
            {
                return new Response(true,"SERVER RECEIVED RENAME COMMAND");
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "Rename");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class Download implements Callable<Response>
    {
        private final String filenameToDownload;
        public Download(String filenameToDownload)
        {
            this.filenameToDownload = filenameToDownload;
        }
        public Response call()
        {
            try
            {
                return new Response(true,"SERVER RECEIVED DOWNLOAD COMMAND");
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "Download");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class Upload implements Callable<Response>
    {
        private final String filenameToUpload;
        public Upload(String filenameToUpload)
        {
            this.filenameToUpload = filenameToUpload;
        }
        public Response call()
        {
            try
            {
                return new Response(true,"SERVER RECEIVED UPLOAD COMMAND");
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "Upload");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class ProcessCommandFromClient implements Callable<Response>
    {
        private final Command command;
        public ProcessCommandFromClient(Command command)
        {
            this.command = command;
        }
        public Response call()
        {
            try
            {
                switch(command.getArgument(0))
                {
                    case "LIST":
                        return executor.submit(new List()).get();
                    case "DELETE":
                        String filenameToDelete = command.getArgument(1);
                        return executor.submit(new Delete(filenameToDelete)).get();
                    case "RENAME":
                        String filenameToRename = command.getArgument(1);
                        String newFilename = command.getArgument(2);
                        return executor.submit(new Rename(filenameToRename,newFilename)).get();
                    case "DOWNLOAD":
                        String filenameToDownload = command.getArgument(1);
                        return executor.submit(new Download(filenameToDownload)).get();
                    case "UPLOAD":
                        String filenameToUpload = command.getArgument(1);
                        return executor.submit(new Upload(filenameToUpload)).get();
                    case "QUIT CLIENT":
                        currentCommand = new Command("QUIT CLIENT");
                        break;
                    case "QUIT SERVER":
                        currentCommand = new Command("QUIT SERVER");
                        break;
                    case "FORCE QUIT":
                        currentCommand = new Command("FORCE QUIT");
                        break;
                    default:
                        return new Response(false, "COULD NOT COMPLETE REQUEST");
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "ProcessCommandFromClient");
                exception.printStackTrace();
            }
            return null;
        }
    }

    public static class OpenStatusListener implements Runnable
    {
        public void run()
        {
            try
            {
                while(!(currentCommand.equals("QUIT CLIENT") || currentCommand.equals("QUIT SERVER") || currentCommand.equals("FORCE QUIT")))
                {
                    Thread.sleep(1000);
                }
                switch(currentCommand.getArgument(0))
                {
                    case "QUIT CLIENT":
                        System.out.println("SHUTTING DOWN CLIENT...");
                        System.out.println("WAITING FOR TASKS TO COMPLETE...");
                        //CHECK IF BEING CALLED FROM CLIENT, THEN TRY TO STOP ACTIVE THREADS
                        if(!executor.awaitTermination(10, TimeUnit.SECONDS))
                        {
                            Thread.currentThread().interrupt();
                            System.out.println("COULD NOT SAFELY SHUTDOWN CLIENT.");
                            System.err.println("FORCE QUITTING...");
                            System.exit(1);
                        }
                        break;
                    case "QUIT SERVER":
                        System.out.println("SHUTTING DOWN SERVER...");
                        System.out.println("WAITING FOR TASKS TO COMPLETE...");
                        //CHECK IF BEING CALLED FROM SERVER, THEN TRY TO STOP ACTIVE THREADS
                        if(!executor.awaitTermination(10, TimeUnit.SECONDS))
                        {
                            Thread.currentThread().interrupt();
                            System.out.println("COULD NOT SAFELY SHUTDOWN SERVER.");
                            System.err.println("FORCE QUITTING...");
                            System.exit(1);
                        }
                        break;
                    case "FORCE QUIT":
                        System.err.println("FORCE QUITTING...");
                        //SHUT DOWN THE EXECUTOR FIRST, THEN SYSTEM.EXIT
                        System.exit(1);
                        break;
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD: " + "OpenStatusListener");
                exception.printStackTrace();
            }
        }
    }
}