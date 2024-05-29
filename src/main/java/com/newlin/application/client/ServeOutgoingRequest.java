package com.newlin.application.client;

import com.newlin.util.command.Command;
import com.newlin.util.response.Response;
import com.newlin.util.filesystem.FileNode;
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
            OldClient.objectOutputStream.writeObject(command);
            Response receivedResponse = (Response) OldClient.objectInputStream.readObject();

            //PROCESS RESPONSE
            if(receivedResponse.data() instanceof FileNode response)
            {
                response.printAll();
            }
            else
            {
                System.out.println(receivedResponse.data());
            }
        }
        catch(IOException | ClassNotFoundException exception)
        {
            System.err.println(OldClient.log("EXCEPTION IN SERVE OUTGOING REQUEST THREAD"));
            exception.printStackTrace();
        }
        finally
        {
            OldClient.queueSlot.release();
        }
    }
}