package org.radarcns.schema.util;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarcns.schema.CommandLineApp;

/** Subcommand of the command line utility. */
public interface SubCommand {
    /** Name of the command to be used on the command line. */
    String getName();

    /**
     * Execute the subcommand based on the options and app given.
     * @param options the options passed on the command line.
     * @param app application with source catalogue.
     * @return command exit code.
     */
    int execute(Namespace options, CommandLineApp app);

    /**
     * Add the description and arguments for this sub-command to the argument parser. The values
     * of the arguments will then be passed to {@link #execute(Namespace, CommandLineApp)}.
     *
     * @param parser argument parser of the current subcommand.
     */
    void addParser(ArgumentParser parser);

    /**
     * Adds the root directory as an argument. This should be called as the last option,
     * if at all.
     */
    static void addRootArgument(ArgumentParser parser) {
        parser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
    }
}
