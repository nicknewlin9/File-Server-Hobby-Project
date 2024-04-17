package com.newlin.application;

import java.io.Serializable;

public class Command implements Serializable
{
    private final Actions action;
    private final String[] args;

    public Command(Actions action, String... args)
    {
        this.action = action;
        this.args = new String[this.action.NUM_ARGS];
        System.arraycopy(args, 0, this.args, 0, action.NUM_ARGS);
    }
    public Command(Actions action)
    {
        this.action = action;
        this.args = new String[]{};
    }

    public Actions getAction()
    {
        return this.action;
    }
    public String[] getArgs()
    {
        return this.args;
    }
}