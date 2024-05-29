package com.newlin.util.filesystem;

import com.newlin.util.command.Command;
import com.newlin.util.response.Response;
import com.newlin.application.server.OldServer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandProcessor
{
    public static Response ProcessListCommand(Command command)
    {
        try
        {
            String pathString = command.args()[0];
            if(pathString.equals("\n"))
            {
                return new Response(true, OldServer.fileSystem.getRootNode());
            }
            else
            {
                Path path = Paths.get(command.args()[0]);
                return new Response(true, OldServer.fileSystem.getRootNode());
            }
        }
        catch(Exception exception)
        {
            System.err.println(OldServer.log("EXCEPTION PROCESSING LIST COMMAND"));
            exception.printStackTrace();
        }
        return new Response(false, "NOT SLAY");
    }

    public static Response ProcessDeleteCommand(Command command)
    {
        Response response = new Response(false, OldServer.log("COULDN'T DELETE UPLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, OldServer.log("RECEIVED DELETE COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(OldServer.log("EXCEPTION PROCESSING DELETE COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessRenameCommand(Command command)
    {
        Response response = new Response(false, OldServer.log("COULDN'T PROCESS RENAME COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, OldServer.log("RECEIVED RENAME COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(OldServer.log("EXCEPTION PROCESSING RENAME COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessDownloadCommand(Command command)
    {
        Response response = new Response(false, OldServer.log("COULDN'T PROCESS DOWNLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, OldServer.log("RECEIVED DOWNLOAD COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(OldServer.log("EXCEPTION PROCESSING DOWNLOAD COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }

    public static Response ProcessUploadCommand(Command command)
    {
        Response response = new Response(false, OldServer.log("COULDN'T PROCESS UPLOAD COMMAND"));
        try
        {
            //GENERATE RESPONSE
            response = new Response(true, OldServer.log("RECEIVED UPLOAD COMMAND"));
        }
        catch(Exception exception)
        {
            System.err.println(OldServer.log("EXCEPTION PROCESSING UPLOAD COMMAND"));
            exception.printStackTrace();
        }
        return response;
    }
}
