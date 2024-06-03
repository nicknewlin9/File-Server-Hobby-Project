package com.newlin.util.command;

import com.newlin.util.response.Response;

public class CommandProcessor
{
    public Response submit(Command command)
    {
        return new Response(true, command);
    }
}
