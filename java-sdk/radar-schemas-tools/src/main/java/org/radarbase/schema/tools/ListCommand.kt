package org.radarbase.schema.tools

import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import java.util.stream.Collectors
import java.util.stream.Stream

class ListCommand : SubCommand {
    override val name: String = "list"

    override suspend fun execute(options: Namespace, app: CommandLineApp): Int {
        val out: Stream<String> = when {
            options.getBoolean("raw") -> app.rawTopics
            options.getBoolean("stream") -> app.resultsCacheTopics
            options.getBoolean("quiet") -> app.topicsToCreate
            else -> app.getTopicsVerbose(true, options.getString("match"))
        }
        println(
            out
                .sorted()
                .distinct()
                .collect(Collectors.joining("\n")),
        )
        return 0
    }

    override fun addParser(parser: ArgumentParser) {
        parser.apply {
            description("list topics and schemas")
            addArgument("-r", "--raw")
                .help("list raw input topics")
                .action(Arguments.storeTrue())
            addArgument("-q", "--quiet")
                .help("only print the requested topics")
                .action(Arguments.storeTrue())
            addArgument("-m", "--match")
                .help("only print the requested topics")
            addArgument("-S", "--stream")
                .help("list the output topics of Kafka Streams")
                .action(Arguments.storeTrue())
            addRootArgument()
        }
    }
}
