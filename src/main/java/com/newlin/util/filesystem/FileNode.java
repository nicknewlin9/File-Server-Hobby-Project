package com.newlin.util.filesystem;

import com.newlin.util.logger.ConsoleColors;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a file or directory.
 */
public class FileNode implements Serializable
{
    /**
     * The name of the file or directory.
     */
    private String fileName;

    /**
     * If the {@code FileNode} is a directory.
     */
    private boolean isDirectory;

    /**
     * The parent directory of the {@code FileNode}.
     */
    private FileNode parent;

    /**
     * A list (initialized as an {@code ArrayList}) of files and directories ({@code FileNode} objects)
     * contained within the current {@code FileNode}.
     * <p>
     * If this {@code FileNode} represents a file, the list will be empty.
     */
    private final List<FileNode> children = new ArrayList<>();

    /**
     * {@code FileNode} constructor that takes the name of the file or directory as a string.
     * @param fileName the name of the file or directory.
     */
    public FileNode(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * Gets the name of the file or directory represented by this {@code FileNode}.
     * @return the name of the file or directory represented by this {@code FileNode}.
     */
    protected String getFileName()
    {
        return this.fileName;
    }

    /**
     * Sets the name of the file or directory represented by this {@code FileNode}.
     * @param fileName the name of the file or directory represented by this {@code FileNode}.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * Gets the parent {@code FileNode} of this {@code FileNode}.
     * @return the {@code FileNode} whose list of {@code children} contains this {@code FileNode}.
     * The root {@code FileNode} of a file system will return {@code null}.
     */
    protected FileNode getParent()
    {
        return this.parent;
    }

    /**
     * Sets the parent {@code FileNode} for this {@code FileNode}.
     * @param parent the {@code FileNode} that is 1 step above this {@code FileNode} in the file structure.
     */
    protected void setParent(FileNode parent)
    {
        this.parent = parent;
    }

    /**
     * Gets the list of {@code FileNode} that are {@code children} of this {@code FileNode}.
     * @return the list of {@code FileNode} that are {@code children} of this {@code FileNode}.
     */
    public List<FileNode> getChildren()
    {
        return this.children;
    }

    /**
     * Returns a {@code boolean} that represents if a {@code FileNode} represents a directory or a file.
     * @return {@code true} if the {@code FileNode} represents a directory (a folder) and {@code false} if it is a file.
     */
    public boolean isDirectory()
    {
        return isDirectory;
    }

    /**
     * Sets this {@code FileNode} {@code isDirectory} field.
     * @param isDirectory set to {@code true} if the {@code FileNode} represents a directory (a folder)
     * and {@code false} if it is a file.
     */
    public void setIsDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    /**
     * Adds a child to this {@code FileNode} list of children.
     * <p>
     * Sets the {@code parent} field of the child to this {@code FileNode}.
     * @param child {@code FileNode} to be added to this {@code FileNode} list of children.
     */
    protected void addChild(FileNode child)
    {
        child.setParent(this);
        this.children.add(child);
    }

    /**
     * Gets the {@code Path} of this {@code FileNode}.
     * <p>
     * It could be relative to the project's root directory or an absolute path.
     * @return the {@code Path} of this {@code FileNode}.
     */
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

    /**
     * Creates a new {@code FileNode} that represents the file system and recursively calls itself to build the
     * tree structure of the file system.
     * <p>
     * In general, this method should be called with a local path (relative to the project's root directory).
     * @param path the {@code Path} to the file or directory. If this is an absolute path, this method should only be
     * called on the local machine since Path objects are not serializable. A path relative to the project's root
     * directory is preferred because it avoids issues with the {@code Files.isDirectory(Path path)} method when
     * labeling the {@code isDirectory} field.
     * @return a new {@code FileNode} that represents the files and directories in the file system.
     * If the {@code Path} is not valid, it will return a {@code FileNode} with an empty list of {@code children}.
     */
    public static FileNode loadFileStructure(Path path)
    {
        FileNode node = new FileNode(path.getFileName().toString());

        if (Files.isDirectory(path))
        {
            node.setIsDirectory(true);
            try (Stream<Path> pathStream = Files.list(path))
            {
                List<Path> sortedPaths = pathStream
                        .filter(p -> !p.getFileName().toString().startsWith("."))
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

    /**
     * Builds a {@code String} that represents this {@code FileNode} and all of its descendant {@code children}.
     * @param node the {@code FileNode} to convert.
     * @return a {@code String} that represents this {@code FileNode}.
     */
    public String getNodeString(FileNode node)
    {
        StringBuilder output = new StringBuilder();

        output.append("=".repeat(64)).append("\n");
        output.append(getNodeString(node,0));
        output.append("=".repeat(64)).append("\n");

        return output.toString();
    }

    /**
     * Private utility method that builds a {@code String} that represents this {@code FileNode} and recursively adds
     * all descendant {@code children}.
     * @param node the {@code FileNode} to convert.
     * @param level the depth of the {@code FileNode} in the file system (root node has level 0).
     * @return a {@code String} that represents this {@code FileNode}.
     */
    private String getNodeString(FileNode node, int level)
    {
        StringBuilder output = new StringBuilder();
        output.append("   ".repeat(Math.max(0, level)));
        if (node.isDirectory())
        {
            if(level == 0)
            {
                output.append("[").append(node.getPath().getFileName()).append("]").append("\n");
            }
            else
            {
                output.append("<> [").append(node.getPath().getFileName()).append("]").append("\n");
            }
        }
        else
        {
            output.append(" > ").append(node.getPath().getFileName()).append("\n");
        }

        List<FileNode> directories = new ArrayList<>();
        List<FileNode> files = new ArrayList<>();
        for (FileNode child : node.getChildren())
        {
            if (child.isDirectory())
            {
                directories.add(child);
            }
            else
            {
                files.add(child);
            }
        }

        files = files.stream()
                .sorted((f1, f2) -> f1.getFileName().compareToIgnoreCase(f2.getFileName()))
                .collect(Collectors.toList());

        directories = directories.stream()
                .sorted((d1, d2) -> d1.getFileName().compareToIgnoreCase(d2.getFileName()))
                .collect(Collectors.toList());

        for (FileNode file : files)
        {
            output.append(getNodeString(file, level + 1));
        }
        for (FileNode directory : directories)
        {
            output.append(getNodeString(directory, level + 1));
        }
        return output.toString();
    }

    /**
     * Builds a {@code String} that represents this {@code FileNode} and its direct {@code children}.
     * @return the {@code String} that represents this {@code FileNode} and only its direct children.
     */
    public String getChildrenString()
    {
        StringBuilder output = new StringBuilder();

        output.append("=".repeat(64)).append("\n");

        if (isDirectory())
        {
            output.append("[").append(getPath().getFileName()).append("]").append("\n");
        }
        else
        {
            output.append(" > ").append(getPath().getFileName()).append("\n");
        }

        List<FileNode> directories = new ArrayList<>();
        List<FileNode> files = new ArrayList<>();
        for (FileNode child : getChildren())
        {
            if (child.isDirectory())
            {
                directories.add(child);
            }
            else
            {
                files.add(child);
            }
        }

        files = files.stream()
                .sorted((f1, f2) -> f1.getFileName().compareToIgnoreCase(f2.getFileName()))
                .collect(Collectors.toList());

        directories = directories.stream()
                .sorted((d1, d2) -> d1.getFileName().compareToIgnoreCase(d2.getFileName()))
                .collect(Collectors.toList());

        for (FileNode directory : directories)
        {
            output.append("    ").append("<> [").append(directory.getPath().getFileName()).append("]").append("\n");
        }
        for (FileNode file : files)
        {
            output.append("    ").append(" > ").append(file.getPath().getFileName()).append("\n");
        }

        output.append("=".repeat(64)).append("\n");
        return output.toString();
    }

    /**
     * Builds a console-friendly {@code String} that represents this {@code FileNode} and its direct {@code children}.
     * <p>
     * The files and directories are color coded with ANSI escape codes as listed in
     * {@code com.newlin.util.logger.ConsoleColors}.
     * @return the {@code String} that represents this {@code FileNode} and only its direct children.
     */
    public String getChildrenStringForConsole()
    {
        StringBuilder output = new StringBuilder();

        output.append("=".repeat(64)).append("\n");

        if (isDirectory())
        {
            output.append("[").append(getPath().getFileName()).append("]").append("\n");
        }
        else
        {
            output.append(" > ").append(getPath().getFileName()).append("\n");
        }

        List<FileNode> directories = new ArrayList<>();
        List<FileNode> files = new ArrayList<>();
        for (FileNode child : getChildren())
        {
            if (child.isDirectory())
            {
                directories.add(child);
            }
            else
            {
                files.add(child);
            }
        }

        files = files.stream()
                .sorted((f1, f2) -> f1.getFileName().compareToIgnoreCase(f2.getFileName()))
                .collect(Collectors.toList());

        directories = directories.stream()
                .sorted((d1, d2) -> d1.getFileName().compareToIgnoreCase(d2.getFileName()))
                .collect(Collectors.toList());

        for (FileNode directory : directories)
        {
            output.append("    ").append("<> [").append(directory.getPath().getFileName()).append("]").append("\n");
        }
        for (FileNode file : files)
        {
            output.append("    ").append(" > ").append(file.getPath().getFileName()).append("\n");
        }

        output.append("=".repeat(64)).append("\n");
        return output.toString();
    }

    /**
     * @return the {@code String} representation of this {@code FileNode} including all descendant children.
     */
    @Override
    public String toString()
    {
        return getNodeString(this);
    }
}