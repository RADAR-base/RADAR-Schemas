package org.radarbase.schema.tools;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class ListCommand implements SubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        Stream<String> out;
        if (options.getBoolean("raw")) {
            out = app.getRawTopics();
        } else if (options.getBoolean("stream")) {
            out = app.getResultsCacheTopics();
        } else if (options.getBoolean("quiet")) {
            out = app.getTopicsToCreate();
        } else {
            out = app.getTopicsVerbose(true, options.getString("match"));
        }
        System.out.println(out.sorted().distinct()
                .collect(Collectors.joining("\n")));
        return 0;
    }

    @Override
    public void addParser(ArgumentParser parser) {
        parser.description("list topics and schemas");
        parser.addArgument("-r", "--raw")
                .help("list raw input topics")
                .action(Arguments.storeTrue());
        parser.addArgument("-q", "--quiet")
                .help("only print the requested topics")
                .action(Arguments.storeTrue());
        parser.addArgument("-m", "--match")
                .help("only print the requested topics");
        parser.addArgument("-S", "--stream")
                .help("list the output topics of Kafka Streams")
                .action(Arguments.storeTrue());
        SubCommand.addRootArgument(parser);
    }
}
