package org.radarbase.schema.tools

import net.sourceforge.argparse4j.inf.ArgumentChoice
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace

/**
 * Subcommand of the command line utility.
 */
interface SubCommand {
    /**
     * Name of the command to be used on the command line.
     */
    val name: String

    /**
     * Execute the subcommand based on the options and app given.
     *
     * @param options the options passed on the command line.
     * @param app     application with source catalogue.
     * @return command exit code.
     */
    suspend fun execute(options: Namespace, app: CommandLineApp): Int

    /**
     * Add the description and arguments for this sub-command to the argument parser. The values of
     * the arguments will then be passed to [.execute].
     *
     * @param parser argument parser of the current subcommand.
     */
    fun addParser(parser: ArgumentParser)

    class IntRangeArgumentChoice(private val minRange: Int, private val maxRange: Int) :
        ArgumentChoice {
        override fun contains(value: Any): Boolean {
            return value is Int && value >= minRange && value <= maxRange
        }

        override fun textualFormat(): String {
            return "[allowed range: $minRange-$maxRange]"
        }
    }

    companion object {
        /**
         * Adds the root directory as an argument. This should be called as the last option, if at all.
         */
        fun ArgumentParser.addRootArgument() {
            addArgument("-c", "--config")
                .help("Configuration YAML file")
                .type(String::class.java)

            addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .default = "."
        }
    }
}
