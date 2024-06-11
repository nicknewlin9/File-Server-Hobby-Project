package com.newlin.util.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileSystem
{
    public static FileNode createFileStructure(Path path)
    {
        FileNode node = new FileNode(path.getFileName().toString());

        if (Files.isDirectory(path))
        {
            node.setIsDirectory(true);
            try (Stream<Path> pathStream = Files.list(path))
            {
                List<Path> sortedPaths = pathStream
                        .filter(p -> !p.getFileName().toString().startsWith("."))
                        .sorted(Comparator.comparing(p -> !Files.isDirectory(p)))
                        .toList();

                for (Path p : sortedPaths)
                {
                    FileNode childNode = createFileStructure(p);
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


}
