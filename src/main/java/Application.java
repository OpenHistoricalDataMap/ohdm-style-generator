import cli.GeneratorCommand;
import picocli.CommandLine;

public class Application {

    public int run(String[] args) {
        int exitCode = new CommandLine(new GeneratorCommand()).execute(args);
        return exitCode;
    }
}
