package com.newlin.test;

import org.junit.jupiter.api.Test;

public class ListCommandTest
{
    @Test
    public void bitch()
    {
        // ANSI escape code for blue text
        final String BLUE = "\033[0;34m";
        final String RESET = "\033[0m";  // Reset to default color

        // Print "Hello world" in blue text
        System.out.println(BLUE + "Hello world" + RESET);

        // ANSI escape code for blue text
        final String BLUEf = "\u001B[34m";
        final String RESETf = "\u001B[0m";  // Reset to default color

        // Print "Hello world" in blue text
        System.out.println(BLUEf + "Hello world" + RESETf);
    }
}
