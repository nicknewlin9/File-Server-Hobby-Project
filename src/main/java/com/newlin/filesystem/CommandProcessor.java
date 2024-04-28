package com.newlin.filesystem;

import com.newlin.application.Command;
import com.newlin.application.Response;
import com.newlin.application.server.Server;

public class CommandProcessor
{
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
