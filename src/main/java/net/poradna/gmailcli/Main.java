package net.poradna.gmailcli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    private static final Options cliOptions = new Options();
    private static final GmailService gmailService = new GmailService();

    public static void main(String[] args) {
        buildCli();
        CommandLine commandLine = parseCli(args);
        if (commandLine == null) {
            return;
        }

        String recipient = commandLine.getOptionValue("recipient");
        String subject = commandLine.getOptionValue("subject");
        String bodyText = String.format(commandLine.getOptionValue("body"));
        File attachment = null;
        if (commandLine.hasOption("attachment")) {
            attachment = new File(commandLine.getOptionValue("attachment"));
        }

        if (gmailService.sendMessage(recipient, subject, bodyText, attachment)) {
            System.out.println("Email sent!");
        } else {
            System.err.print("Email not sent!");
        }
    }

    private static void buildCli() {
        Option o1 = new Option("b", "body", true, "body text of email");
        o1.setRequired(true);
        o1.setArgName("BODY_TEXT");

        Option o2 = new Option("s", "subject", true, "subject of email");
        o2.setRequired(true);
        o2.setArgName("SUBJECT");

        Option o3 = new Option("r", "recipient", true, "recipient of email");
        o3.setArgName("EMAIL");
        o3.setRequired(true);

        Option o4 = new Option("a", "attachment", true, "path to attachment of email");
        o4.setArgName("FILE");

        cliOptions.addOption(o1);
        cliOptions.addOption(o2);
        cliOptions.addOption(o3);
        cliOptions.addOption(o4);
    }

    private static CommandLine parseCli(final String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(cliOptions, args);
        } catch (MissingOptionException moe) {
            System.err.println("Missing required options: " + moe.getMissingOptions());
            printHelp();
        } catch (MissingArgumentException mae) {
            System.err.println("Option -" + mae.getOption().getOpt() + " is missing argument " + mae.getOption().getArgName());
            printHelp();
        } catch (ParseException e) {
            e.printStackTrace();
            printHelp();
        }
        return null;
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar GmailCli", cliOptions);
    }
}