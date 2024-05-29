package com.newlin.util.filesystem;

import com.newlin.util.logger.ConsoleColors;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileNode implements Serializable
{
    private transient Path path;
    private String pathString;
    private FileNode parent;
    private List<FileNode> children;

    public FileNode(Path path)
    {
        this.path = path;
        pathString = this.path.toString();
        children = new ArrayList<>();
    }

    public void addChild(FileNode child)
    {
        this.children.add(child);
    }

    public void setParent(FileNode parent)
    {
        this.parent = parent;
    }

    public FileNode getParent()
    {
        return parent;
    }

    public Path getPath()
    {
        if (path == null)
        {
            path = Paths.get(pathString);
        }
        return path;
    }

    public String getPathString()
    {
        return pathString;
    }

    public List<FileNode> getChildren()
    {
        return children;
    }

    public void setChildren(List<FileNode> children)
    {
        this.children = children;
    }

    public void printAll()
    {
        int level = 0;
        printAll(this,level);
    }

    private void printAll(FileNode node, int level)
    {
        for(int i = 0; i < level; i++)
        {
            System.out.print("    ");
        }
        if(node.getPath().toFile().isDirectory())
        {
            System.out.println(ConsoleColors.BLUE.getCode() + node.getPath().getFileName() + ConsoleColors.RESET.getCode());
        }
        else
        {
            System.out.println(node.getPath().getFileName());
        }
        node.getChildren().forEach(child -> printAll(child, level + 1));
    }

    public void printChildren()
    {
        for (FileNode child : getChildren())
        {
            if(child.getPath().toFile().isDirectory())
            {
                System.out.println(ConsoleColors.BLUE.getCode() + child.getPath().getFileName() + ConsoleColors.RESET.getCode());
            }
            else
            {
                System.out.print("    ");
                System.out.println(child.getPath().getFileName());
            }
        }
    }
}