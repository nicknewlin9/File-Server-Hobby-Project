import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

//CONTAINS RUNNABLE OR CALLABLE ACTIONS
//CLIENT AND SERVER
public class TaskManager
    //MAYBE MAKE TASKMANAGER A CLASS WHERE YOU SET INSTANCE VARIABLE EXECUTORSERVICE AND DEPENDING ON WHAT METHOD YOU
    //CALL, TASKMANAGER WILL CREATE AN INSTANCE OF THAT CLASS AND RUN OR CALL IT

{
    public static ExecutorService executor;
    public TaskManager(ExecutorService executor)
    {
        TaskManager.executor = executor;
    }
    public void setExecutor(ExecutorService executor)
    {
        TaskManager.executor = executor;
    }
    public ExecutorService getExecutor()
    {
        return TaskManager.executor;
    }

    //THIS RUNNABLE CLASS WILL OPEN A LISTENING SOCKET, THEN CONTINUOUSLY ACCEPT NEW CLIENT CONNECTIONS AND
    //CREATE A SERVECLIENTREQUEST TASK TO SERVE EACH NEW SOCKET CONNECTION
    //SERVER
    public static class OpenListenSocket implements Runnable
    {
        private final int SERVER_PORT;
        public OpenListenSocket(int port)
        {
            this.SERVER_PORT = port;
        }
        public void run()
        {
            try
            {
                ServerSocket listenSocket = new ServerSocket(SERVER_PORT);
                while(true)
                {
                    //WAITS FOR CLIENT CONNECTIONS
                    Socket socket = listenSocket.accept();
                    System.out.println("A client has connected.");
                    executor.submit(new ServeClientRequest(socket)); //NEED TO TIMEOUT THE THREAD AND DISCONNECT CLIENT IF NO COMMAND RECEIVED
                }
            }
            catch(SocketTimeoutException exception)
            {
                System.err.println("SOCKET TIMED OUT");
                exception.printStackTrace();
                System.exit(0);
            }
            catch(IOException exception)
            {
                System.err.println("CAN'T OPEN LISTEN SOCKET AND SERVE CLIENTS");
                exception.printStackTrace();
                System.exit(0);
            }
        }
    }

    //THIS CALLABLE CLASS TAKES A SERVER IP AND A DESTINATION PORT TO CONNECT THE CLIENT TO THE SERVER
    //IT RETURNS A SOCKET REPRESENTING THE CONNECTION
    //CLIENT
    public static class ConnectClient implements Callable<Socket>
    {
        private final String SERVER_IP;
        private final int SERVER_PORT;
        public ConnectClient(String SERVER_IP, int SERVER_PORT)
        {
            this.SERVER_IP = SERVER_IP;
            this.SERVER_PORT = SERVER_PORT;
        }
        public Socket call()
        {
            Socket socket = new Socket();
            try
            {
                InetAddress IPAddress = InetAddress.getByName(SERVER_IP);
                InetSocketAddress socketAddress = new InetSocketAddress(IPAddress, SERVER_PORT);
                socket.connect(socketAddress);
                return socket;
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
            return socket;
        }
    }

    //THIS RUNNABLE CLASS WILL RECEIVE A PACKET, GET THE COMMAND FROM THE PACKET, SUBMIT THE COMMAND TO THE
    //COMMAND PROCESSOR, GET THE RESPONSE FROM THE COMMAND PROCESSOR, THEN SEND A PACKET WITH THE RESPONSE TO THE CLIENT
    //SERVER
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

                Response response = executor.submit(new ProcessCommand(receivedCommand)).get();

                Packet<Response> responsePacket = new Packet<>(response);

                executor.submit(new SendPacket(socket, responsePacket));
            }
            catch(ExecutionException exception)
            {
                System.err.println("EXECUTION EXCEPTION IN SERVECLIENTREQUEST THREAD");
                exception.printStackTrace();
                System.exit(0);
            }
            catch(InterruptedException exception)
            {
                System.err.println("INTERRUPTED EXCEPTION IN SERVECLIENTREQUEST THREAD");
                exception.printStackTrace();
                System.exit(0);
            }
        }
    }

    //THIS CALLABLE CLASS WILL TAKE A COMMAND, CREATE A PACKET WITH THE COMMAND, SEND THE PACKET TO THE SERVER
    //WAIT FOR AND ACCEPT A PACKET FROM THE SERVER, THEN RETURNS THE RESPONSE FROM THAT PACKET
    //CLIENT
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
            Response response = new Response(false, "COULD NOT COMPLETE REQUEST");
            try
            {
                Packet<Command> commandPacket = new Packet<>(command);

                executor.submit(new SendPacket(socket,commandPacket));

                Packet<?> receivedPacket = executor.submit(new ReceivePacket(socket)).get();

                response = receivedPacket.getResponseFromPacket();
            }
            catch(ExecutionException exception)
            {
                System.err.println("EXECUTION EXCEPTION");
                exception.printStackTrace();
                System.exit(0);
            }
            catch(InterruptedException exception)
            {
                System.err.println("INTERRUPTED EXCEPTION");
                exception.printStackTrace();
                System.exit(0);
            }
            return response;
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
            }
            catch(IOException exception)
            {
                System.err.println("CAN'T SEND PACKET TO SERVER");
                exception.printStackTrace();
                System.exit(0);
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
                return (Packet<?>) objectInputStream.readObject();
            }
            catch(IOException exception)
            {
                System.err.println("CAN'T GET RESPONSE FROM SERVER");
                exception.printStackTrace();
                System.exit(0);
            }
            catch(ClassNotFoundException exception)
            {
                System.err.println("RESPONSE FROM SERVER INVALID");
                exception.printStackTrace();
                System.exit(0);
            }
            return new Packet<>(null);
        }
    }
    public static class List implements Callable<Response>
    {
        public List()
        {

        }
        public Response call()
        {
            //DO THINGS
            //GENERATE AND RETURN RESPONSE
            return new Response(true,"SERVER RECEIVED LIST COMMAND");
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
            //DO THINGS
            //GENERATE AND RETURN RESPONSE
            return new Response(true,"SERVER RECEIVED DELETE COMMAND");
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
            //DO THINGS
            //GENERATE AND RETURN RESPONSE
            return new Response(true,"SERVER RECEIVED RENAME COMMAND");
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
            //DO THINGS
            //GENERATE AND RETURN RESPONSE
            return new Response(true,"SERVER RECEIVED DOWNLOAD COMMAND");
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
            //DO THINGS
            //GENERATE AND RETURN RESPONSE
            return new Response(true,"SERVER RECEIVED UPLOAD COMMAND");
        }
    }

    //THIS CALLABLE CLASS TAKES A COMMAND, THEN CREATES AND SUBMITS THE TASK TO THE TASK MANAGER
    //IT THEN RETURNS THE RESPONSE FROM THAT TASK
    //SERVER
    public static class ProcessCommand implements Callable<Response>
    {
        private final Command command;
        public ProcessCommand(Command command)
        {
            this.command = command;
        }
        public Response call() throws ExecutionException, InterruptedException
        {
            Response response = new Response(false, "COULD NOT COMPLETE REQUEST");
            switch(command.getArgument(0))
            {
                //WAITS IN CASE BLOCKS FOR RESPONSE
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
                    return new Response(true, "SERVER RECEIVED QUIT CLIENT COMMAND");

                case "QUIT SERVER":
                    return new Response(true, "SERVER RECEIVED QUIT SERVER COMMAND");

                default:
                    return new Response(false, "SERVER RECEIVED UNKNOWN COMMAND");
            }
        }
    }

    public static class GetCommandFromUser implements Callable<Command>
    {
        public GetCommandFromUser()
        {

        }
        public Command call()
        {
            Scanner scanner = new Scanner(System.in);
            Command command;
            do
            {
                System.out.println("\nEnter a command: ");
                switch(scanner.nextLine().toUpperCase())
                {
                    case "LIST":
                        command = new Command("LIST");
                        break;

                    case "DELETE":
                        System.out.println("Type name of file to delete: ");
                        String filenameToDelete = scanner.nextLine();
                        command = new Command("DELETE",filenameToDelete);
                        break;

                    case "RENAME":
                        System.out.println("Type name of file to rename: ");
                        String filenameToRename = scanner.nextLine();
                        System.out.println("Type new file name: ");
                        String newFilename = scanner.nextLine();
                        command = new Command("RENAME",filenameToRename,newFilename);
                        break;

                    case "DOWNLOAD":
                        System.out.println("Type name of file to download: ");
                        String filenameToDownload = scanner.nextLine();
                        command = new Command("DOWNLOAD",filenameToDownload);
                        break;

                    case "UPLOAD":
                        System.out.println("Type name of file to upload: ");
                        String filenameToUpload = scanner.nextLine();
                        command = new Command("UPLOAD",filenameToUpload);
                        break;

                    case "QUIT CLIENT":
                        System.out.println("\nShutting down client...");
                        command = new Command("QUIT CLIENT");
                        break;
                    case "QUIT SERVER":
                        System.out.println("\nShutting down server...");
                        command = new Command("QUIT SERVER");
                    default:
                        System.out.println("\nINVALID COMMAND\nVALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT CLIENT\" OR \"QUIT SERVER\"");
                        command = new Command("INVALID");
                        break;
                }
            }
            while(command.equals("INVALID"));
            scanner.close();
            return command; //SEND THE COMMAND INSTEAD OF RETURNING IT
        }
    }
}