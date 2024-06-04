package com.newlin.util.filesystem;

import com.newlin.util.logger.ConsoleColors;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileNode implements Serializable
{
    private String fileName;
    private FileNode parent;
    private final List<FileNode> children;

    public FileNode(String fileName)
    {
        this.fileName = fileName;
        this.children = new ArrayList<>();
    }

    protected String getFileName()
    {
        return this.fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    protected FileNode getParent()
    {
        return this.parent;
    }

    protected void setParent(FileNode parent)
    {
        this.parent = parent;
    }

    public List<FileNode> getChildren()
    {
        return this.children;
    }

    protected void addChild(FileNode child)
    {
        child.setParent(this);
        this.children.add(child);
    }

    public Path getPath()
    {
        List<String> pathElements = new ArrayList<>();
        FileNode current = this;
        while (current != null)
        {
            pathElements.addFirst(current.getFileName());
            current = current.getParent();
        }
        return Paths.get(String.join("/", pathElements));
    }

    public static FileNode loadFileStructure(Path path)
    {
        FileNode node = new FileNode(path.getFileName().toString());

        if (Files.isDirectory(path))
        {
            try (Stream<Path> pathStream = Files.list(path))
            {
                List<Path> sortedPaths = pathStream
                        .filter(p -> !p.getFileName().toString().startsWith("."))
                        .sorted(Comparator.comparing(p -> !Files.isDirectory(p)))
                        .toList();

                for (Path p : sortedPaths)
                {
                    FileNode childNode = loadFileStructure(p);
                    node.addChild(childNode);
                }
            }
            catch (IOException exception)
            {
                System.err.println("Error reading directory: " + path);
            }
        }
        return node;
    }

    public String getChildrenString()
    {
        StringBuilder output = new StringBuilder();

        output.append("\n");
        output.append("_".repeat(64));
        output.append("\n");

        output.append(ConsoleColors.BLUE.getCode()).append(this.getFileName()).append(ConsoleColors.BRIGHT_WHITE.getCode()).append("\n");
        for (FileNode child : getChildren())
        {
            if (Files.isDirectory(child.getPath()))
            {
                output.append("    ").append(ConsoleColors.BLUE.getCode()).append(child.getFileName()).append(ConsoleColors.BRIGHT_WHITE.getCode()).append("\n");
            }
            else
            {
                output.append("    ").append(child.getFileName()).append("\n");
            }
        }

        output.append("_".repeat(64));
        output.append("\n");

        return output.toString();
    }

    public String getPrintable()
    {
        return "_".repeat(64) + "\n" + getNodeString(this, 0) + "_".repeat(64);
    }

    private String getNodeString(FileNode node, int level)
    {
        StringBuilder output = new StringBuilder();
        output.append("    ".repeat(Math.max(0, level)));
        if (Files.isDirectory(node.getPath()))
        {
            output.append(ConsoleColors.BLUE.getCode()).append(node.getPath().getFileName()).append(ConsoleColors.RESET.getCode()).append("\n");
        }
        else
        {
            output.append(node.getPath().getFileName()).append("\n");
        }
        for (FileNode child : node.getChildren())
        {
            output.append(getNodeString(child, level + 1));
        }
        return output.toString();
    }

    @Override
    public String toString()
    {
        return getNodeString(this, 0);
    }
}