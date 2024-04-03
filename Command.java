import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Command
{
    private final List<String> args = new LinkedList<>();
    public Command(String...args)
    {
        this.args.addAll(Arrays.asList(args));
    }

    public List<String> getArgs()
    {
        return args;
    }

    public String getArgument(int index)
    {
        return args.get(index);
    }

    @Override
    public String toString()
    {
        List<String> stringList = new LinkedList<>();
        stringList.addAll(this.args);
        return stringList.toString();
    }

    public boolean equals(String string)
    {
        return this.args.getFirst().equals(string);
    }
}
