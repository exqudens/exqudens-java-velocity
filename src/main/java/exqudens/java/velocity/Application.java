package exqudens.java.velocity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.dishevelled.commandline.ArgumentList;
import org.dishevelled.commandline.CommandLine;
import org.dishevelled.commandline.CommandLineParser;
import org.dishevelled.commandline.Switch;
import org.dishevelled.commandline.Usage;
import org.dishevelled.commandline.argument.FileArgument;
import org.dishevelled.commandline.argument.StringArgument;
import org.dishevelled.commandline.argument.StringListArgument;

import java.util.List;
import java.util.Properties;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class Application implements Runnable {

    static String USAGE = "velocity -t template.wm\n  [-c foo=bar,baz=qux]\n  [-r /resource/path]\n  [-o output.txt]\n  [-e euc-jp]\n  [-g date,math]\n  [--verbose]";
    List<String> args;

    @Override
    @SneakyThrows
    public void run() {
        // required arguments
        var template = new FileArgument("t", "template", "template file", true);

        // optional arguments
        var help = new Switch("h", "help", "display help message");
        var verbose = new Switch("v", "verbose", "display verbose log messages");
        var context = new StringArgument("c", "context", "context as comma-separated key value pairs", false);
        var output = new FileArgument("o", "output", "output file, default stdout", false);
        var tools = new StringListArgument("g", "tools", "comma-separated list of generic tools to install", false);
        var charset = new StringArgument("e", "encoding", "encoding, default UTF-8", false);

        var arguments = new ArgumentList(help, template, context, output, charset, tools, verbose);
        var commandLine = new CommandLine(args.toArray(new String[0]));

        CommandLineParser.parse(commandLine, arguments);

        if (help.wasFound()) {
            Usage.usage(USAGE, null, commandLine, arguments, System.out);
            System.exit(-2);
        }

        if (verbose.wasFound()) {
            Properties systemProperties = System.getProperties();
            systemProperties.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        }

        var service = new Service(
                context.getValue(),
                template.getValue(),
                output.getValue(),
                tools.getValue(),
                charset.getValue()
        );

        service.run();
    }

}
