package com.newlin.application.client;

import com.newlin.util.command.Action;
import com.newlin.util.command.Command;

import java.util.Scanner;

import static com.newlin.application.client.Client.isOnline;
import static com.newlin.application.client.Client.logger;

public class UserInputListener implements Runnable
{
    public void run()
    {
        try(Scanner scanner = new Scanner(System.in))
        {
            while(isOnline)
            {
                Client.commandSlot.acquire();
                logger.info("Enter a command: ");
                String input = scanner.nextLine();
                logger.fine("User typed: \"" + input + "\"");

                Action action = switch(input.toUpperCase())
                {
                    case "QUIT" -> Action.QUIT;
                    case "FORCE QUIT" -> Action.FORCE_QUIT;
                    case "LIST" -> Action.LIST;
                    case "DELETE" -> Action.DELETE;
                    case "RENAME" -> Action.RENAME;
                    case "DOWNLOAD" -> Action.DOWNLOAD;
                    case "UPLOAD" -> Action.UPLOAD;
                    default -> Action.INVALID;
                };

                Command command = constructCommand(scanner,action);
                if(command.action() != Action.INVALID)
                {
                    Client.executorService.submit(new ServeCommand(command));
                }

            }
        }
        catch(InterruptedException exception)
        {
            logger.severe("Exception while waiting for available command slot");
        }
    }

    public Command constructCommand(Scanner scanner, Action action)
    {
        switch(action)
        {
            case Action.QUIT:
                //Client.offline.signal();
                return new Command(Action.QUIT);

            case Action.FORCE_QUIT:
                Client.offline.signal();
                //System.exit(0);
                return new Command(Action.FORCE_QUIT);

            case Action.LIST:
                logger.info("Directory: ");
                String directory = scanner.nextLine();
                logger.fine("User typed: \"" + directory + "\"");

                return new Command(Action.LIST,directory);

            case Action.DELETE:
                logger.info("Filename to delete: ");
                String filenameToDelete = scanner.nextLine();
                logger.fine("User typed: \"" + filenameToDelete + "\"");

                return new Command(Action.DELETE,filenameToDelete);

            case Action.RENAME:
                logger.info("Filename to rename: ");
                String filenameToRename = scanner.nextLine();
                logger.fine("User typed: \"" + filenameToRename + "\"");
                logger.info("New filename: ");
                String newFilename = scanner.nextLine();
                logger.fine("User typed: \"" + newFilename + "\"");

                return new Command(Action.RENAME,filenameToRename,newFilename);

            case Action.DOWNLOAD:
                logger.info("Filename to download: ");
                String filenameToDownload = scanner.nextLine();
                logger.fine("User typed: \"" + filenameToDownload + "\"");

                return new Command(Action.DOWNLOAD,filenameToDownload);

            case Action.UPLOAD:
                logger.info("Filename to upload: ");
                String filenameToUpload = scanner.nextLine();
                logger.fine("User typed: \"" + filenameToUpload + "\"");

                return new Command(Action.UPLOAD,filenameToUpload);

            default:
                logger.info("Valid commands: \"list\" \"delete\" \"rename\" \"download\" \"upload\" \"quit\" or \"force quit\"");
                return new Command(Action.INVALID);
        }
    }
}