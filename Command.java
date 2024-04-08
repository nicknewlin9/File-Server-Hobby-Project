import java.io.Serializable;

public class Command implements Serializable
{
    private String command;
    private String[] args;
    public Command(String command, String... args)
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
    public String[] getArgs()
    {
        return args;
    }
    public void setCommand(String command)
    {
        this.command = command;
    }
    public void setArgs(String[] args)
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
        String argsString = "";
        for(int i = 1; i <= args.length; i++)
        {
            argsString = argsString.concat("ARG" + i + "= ").concat(args[i].concat("\n"));
        }
        return"COMMAND: " + command + "\n" + argsString;
    }
}