import com.newlin.filesystem.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.newlin.application.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ListCommandTest
{
    public static void main(String[] args) throws Exception
    {
        List<Path> fileList = getFilesAndDirectories("remote/logs");

        for(Path path : fileList)
        {
            System.out.println(path);
        }


    }

    public static Node buildFileTree(Path path) throws IOException
    {
        Node node = new Node(path);

        if (Files.isDirectory(path))
        {
            try
            {
                Files.list(path).forEach(p ->
                {
                    try
                    {
                        node.addChild(buildFileTree(p));
                    }
                    catch (IOException e)
                    {
                        System.err.println("Error accessing: " + p);
                    }
                });
            }
            catch (IOException e)
            {
                System.err.println("Error reading directory: " + path);
            }
        }
        return node;
    }

    public static void printTree(Node node, int level)
    {
        for (int i = 0; i < level; i++)
        {
            System.out.print("    ");
        }
        System.out.println(node.getPath().getFileName());
        node.getChildren().forEach(child -> printTree(child, level + 1));
    }

    public static List<Path> getFilesAndDirectories(String pathString) throws Exception
    {
        List<Path> directories = new LinkedList<>();
        List<Path> files = new LinkedList<>();
        List<Path> paths = new LinkedList<>();

        DirectoryStream<Path> rootPath = Files.newDirectoryStream(Paths.get(pathString));

        for (Path path : rootPath)
        {
            if(Files.isDirectory(path))
            {
                directories.add(path.getFileName());
            }
            else
            {
                files.add(path.getFileName());
            }
        }
        paths.addAll(directories);
        paths.addAll(files);
        return paths;
    }

    @Test
    public void remote_file_exists() throws IOException
    {
        Path root = Paths.get("remote");

        if(!Files.exists(root))
        {
            Files.createDirectory(root);
        }

        Assertions.assertTrue(Files.isDirectory(root));

    }

    @Test
    public void poopy()
    {
        Path startPath = Paths.get("remote");
        try
        {
            Node root = buildFileTree(startPath);
            printTree(root, 0);
        }
        catch (IOException e)
        {
            System.err.println("Error building the file tree: " + e.getMessage());
        }
    }
}
