import java.io.Serializable;
import java.util.List;

public class Command implements Serializable
{
    private String command;
    private List<String> args;
    public Command(String command, List<String> args)
    {
        this.command = command;
        this.args = args;
    }
    public Command(String command)
    {
        this.command = command;
    }
    public String getCommand()
    {
        return command;
    }
    public List<String> getArgs()
    {
        return args;
    }
    public void setCommand(String command)
    {
        this.command = command;
    }
    public void setArgs(List<String> args)
    {
        this.args = args;
    }
    public boolean equals(String command)
    {
        return this.command.equals(command);
    }
    @Override
    public String toString()
    {
        String stringOfArgs = "\n";
        for(int i = 1; i <= args.size(); i++)
        {
            stringOfArgs = stringOfArgs.concat("ARG" + i + "= ").concat(args.get(i));
        }
        return"COMMAND: " + command + stringOfArgs;
    }
}
