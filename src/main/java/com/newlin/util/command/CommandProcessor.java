package com.newlin.util.command;

import com.newlin.util.filesystem.FileSystem;
import com.newlin.util.response.Response;

public class CommandProcessor
{
    public FileSystem fileSystem;

    public CommandProcessor(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    public Response submit(Command command)
    {
        return new Response(true, command);
    }
}
