package com.newlin.application.server;

import com.newlin.util.command.Command;
import com.newlin.util.response.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServeClient implements Runnable
{
    public Socket socket;
    public String clientName;
    public boolean isConnected = true;

    public ServeClient(Socket socket)
    {
        this.socket = socket;
        this.clientName = socket.getInetAddress().getHostName();
    }

    public void run()
    {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream()))
        {
            while(isConnected)
            {
                if(!socket.isConnected())
                {
                    isConnected = false;
                    return; //SKIPS TO FINALLY BLOCK
                }
                Server.commandSlot.acquire();

                Command receivedCommand = (Command) objectInputStream.readObject();
                Response response = new Response(true, receivedCommand);
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
            }
        }
        catch(ClassNotFoundException exception)
        {
            Server.logger.warning("Can't parse input from client: " + clientName + ". Disconnecting client...");
        }
        catch(InterruptedException exception)
        {
            Server.logger.warning("Exception while waiting for available command slot");
        }
        catch(IOException exception)
        {
            Server.logger.warning("Exception while communicating with client");
        }
        finally
        {
            Server.queueSlot.release();
        }
    }
}