package com.newlin.util.command;

import java.io.Serializable;

public record Command(Action action, String... args) implements Serializable {}