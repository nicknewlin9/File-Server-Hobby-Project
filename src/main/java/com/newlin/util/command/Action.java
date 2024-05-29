package com.newlin.util.command;

@SuppressWarnings("unused")
public enum Action
{
    STARTUP(0),
    INVALID(0),
    CONNECT(1),
    QUIT(0),
    FORCE_QUIT(0),
    LIST(1),
    DELETE(1),
    RENAME(2),
    DOWNLOAD(1),
    UPLOAD(1);

    final int NUM_ARGS;

    Action(int NUM_ARGS)
    {
        this.NUM_ARGS = NUM_ARGS;
    }
}
