package com.newlin.application.server;

import com.newlin.util.command.Action;
import com.newlin.util.command.Command;
import com.newlin.util.filesystem.FileNode;
import com.newlin.util.response.Response;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommandProcessor
{
    public FileNode rootNode;

    public CommandProcessor(FileNode rootNode)
    {
        this.rootNode = rootNode;
    }

    public Response submit(Command command)
    {
        return switch(command.action())
        {
            case Action.QUIT -> quit(command);
            case Action.FORCE_QUIT -> forceQuit(command);
            case Action.LIST -> list(command);
            case Action.DELETE -> delete(command);
            case Action.RENAME -> rename(command);
            case Action.DOWNLOAD -> download(command);
            case Action.UPLOAD -> upload(command);
            default -> new Response(false, null);
        };
    }

    private Response quit(Command command)
    {
        return new Response(true, command);
    }

    private Response forceQuit(Command command)
    {
        return new Response(true, command);
    }

    private Response list(Command command)
    {
        String directory =  rootNode.getPath() + "/" + command.args()[0];

        return new Response(true, findFileNodeByName(directory));
    }

    private Response delete(Command command)
    {
        return new Response(true, command);
    }

    private Response rename(Command command)
    {
        return new Response(true, command);
    }

    private Response download(Command command)
    {
        return new Response(true, command);
    }

    private Response upload(Command command)
    {
        return new Response(true, command);
    }

    public FileNode findFileNodeByName(String searchFileName)
    {
        return searchTreeByPath(rootNode, Paths.get(searchFileName));
    }

    private static FileNode searchTreeByPath(FileNode currentNode, Path path)
    {
        if (currentNode.getPath().equals(path))
        {
            return currentNode;
        }
        List<FileNode> children = currentNode.getChildren();
        if (children != null)
        {
            for (FileNode child : children)
            {
                FileNode result = searchTreeByPath(child, path);
                if (result != null)
                {
                    return result;
                }
            }
        }
        return null;
    }
}
