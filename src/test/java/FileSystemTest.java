import com.newlin.util.filesystem.FileNode;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import com.newlin.util.filesystem.FileSystem;

public class FileSystemTest
{
    @Test
    public void createFileStructure() throws IOException
    {
        FileSystem fileSystem = new FileSystem("src");

        FileNode rootNode = fileSystem.getRootNode();

        rootNode.printAll();

        System.out.println();
        System.out.println();
        System.out.println();

        rootNode.printChildren();

    }
}
