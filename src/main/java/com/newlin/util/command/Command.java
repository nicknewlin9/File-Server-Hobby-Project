package com.newlin.util.command;

import java.io.Serializable;
import java.util.Arrays;

public record Command(Action action, String... args) implements Serializable
{
    @Override
    public String toString()
    {
        return "Command[" +
                "action=" + action +
                ", args=" + Arrays.toString(args) +
                ']';
    }
}