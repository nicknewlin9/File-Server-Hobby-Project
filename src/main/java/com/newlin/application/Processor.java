package com.newlin.application;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class Processor implements Runnable
{
    public Command command;
    public ObjectOutputStream objectOutputStream;

    public Processor(Command command, ObjectOutputStream objectOutputStream)
    {
        this.command = command;
        this.objectOutputStream = objectOutputStream;
    }

    public void run()
    {
        try
        {
            System.out.println(Server.log("NOW PROCESSING " + command.getAction() + " COMMAND"));
            Response response = switch (command.getAction())
            {
                case Command.Actions.CONNECT -> new Response(true, Server.log("CONNECTED SUCCESSFULLY"));

                case Command.Actions.LIST -> ProcessListCommand(command);

                case Command.Actions.DELETE -> ProcessDeleteCommand(command);

                case Command.Actions.RENAME -> ProcessRenameCommand(command);

                case Command.Actions.DOWNLOAD -> ProcessDownloadCommand(command);

                case Command.Actions.UPLOAD -> ProcessUploadCommand(command);

                default -> new Response(false, Server.log("RECEIVED INVALID COMMAND"));
            };
            objectOutputStream.writeObject(response);
            objectOutputStream.flush();
        }
        catch(IOException exception)
        {
            System.err.println(Server.log("EXCEPTION IN THREAD PROCESSING COMMAND"));
            exception.printStackTrace();
        }
        finally
        {
            Server.commandSlot.release();
        }
    }

    public static Response ProcessListCommand(Command command)
    {
        Response response = new Response(false, Server.log("COULDN'T PROCESS LIST COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, Server.log("RECEIVED LIST COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(Server.log("EXCEPTION PROCESSING LIST COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessDeleteCommand(Command command)
    {
        Response response = new Response(false, Server.log("COULDN'T DELETE UPLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, Server.log("RECEIVED DELETE COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(Server.log("EXCEPTION PROCESSING DELETE COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessRenameCommand(Command command)
    {
        Response response = new Response(false, Server.log("COULDN'T PROCESS RENAME COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, Server.log("RECEIVED RENAME COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(Server.log("EXCEPTION PROCESSING RENAME COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessDownloadCommand(Command command)
    {
        Response response = new Response(false, Server.log("COULDN'T PROCESS DOWNLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, Server.log("RECEIVED DOWNLOAD COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(Server.log("EXCEPTION PROCESSING DOWNLOAD COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessUploadCommand(Command command)
    {
        Response response = new Response(false, Server.log("COULDN'T PROCESS UPLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, Server.log("RECEIVED UPLOAD COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(Server.log("EXCEPTION PROCESSING UPLOAD COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }
}
