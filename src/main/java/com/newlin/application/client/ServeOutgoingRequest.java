package com.newlin.application.client;

import com.newlin.application.Command;
import com.newlin.application.Response;
import com.newlin.filesystem.FileNode;
import java.io.IOException;

public class ServeOutgoingRequest implements Runnable
{
    public Command command;

    public ServeOutgoingRequest(Command command)
    {
        this.command = command;
    }

    public void run()
    {
        try
        {
            Client.objectOutputStream.writeObject(command);
            Response receivedResponse = (Response) Client.objectInputStream.readObject();

            //PROCESS RESPONSE
            if(receivedResponse.getResponse() instanceof FileNode response)
            {
                response.printAll();
            }
            else
            {
                System.out.println(receivedResponse.getResponse());
            }
        }
        catch(IOException | ClassNotFoundException exception)
        {
            System.err.println(Client.log("EXCEPTION IN SERVE OUTGOING REQUEST THREAD"));
            exception.printStackTrace();
        }
        finally
        {
            Client.queueSlot.release();
        }
    }
}