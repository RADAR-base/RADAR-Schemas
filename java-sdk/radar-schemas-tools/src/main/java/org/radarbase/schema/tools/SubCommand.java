package org.radarbase.schema.tools;

import net.sourceforge.argparse4j.inf.ArgumentChoice;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Subcommand of the command line utility.
 */
public interface SubCommand {

    /**
     * Adds the root directory as an argument. This should be called as the last option, if at all.
     */
    static void addRootArgument(ArgumentParser parser) {
        parser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
    }

    /**
     * Name of the command to be used on the command line.
     */
    String getName();

    /**
     * Execute the subcommand based on the options and app given.
     *
     * @param options the options passed on the command line.
     * @param app     application with source catalogue.
     * @return command exit code.
     */
    int execute(Namespace options, CommandLineApp app);

    /**
     * Add the description and arguments for this sub-command to the argument parser. The values of
     * the arguments will then be passed to {@link #execute(Namespace, CommandLineApp)}.
     *
     * @param parser argument parser of the current subcommand.
     */
    void addParser(ArgumentParser parser);

    class IntRangeArgumentChoice implements ArgumentChoice {

        private final int minRange;
        private final int maxRange;

        public IntRangeArgumentChoice(int minRange, int maxRange) {
            this.minRange = minRange;
            this.maxRange = maxRange;
        }

        @Override
        public boolean contains(Object val) {
            return val instanceof Integer && (Integer) val >= minRange && (Integer) val <= maxRange;
        }

        @Override
        public String textualFormat() {
            return "[allowed range: " + minRange + "-" + maxRange + "]";
        }
    }
}
