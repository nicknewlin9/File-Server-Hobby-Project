import java.io.Serializable;
public class Command<T extends Serializable> implements Serializable
{
    private T[] array;
    public Command(T[] array)
    {
        this.array = array;
    }

    public T[] getArray()
    {
        return array;
    }
}