package org.radarcns.schema.util;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarcns.schema.CommandLineApp;

public interface SubCommand {
    String getName();

    int execute(Namespace options, CommandLineApp app);

    void addParser(ArgumentParser parser);

    static void addRootArgument(ArgumentParser parser) {
        parser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
    }
}
