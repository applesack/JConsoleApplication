package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.Actuator;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 23:44
 */
public class ConsoleApplication extends AbstractApplication implements Colorful {

    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleConfig config;
    private final Function<String, Actuator> cmdFactory;

    public ConsoleApplication(ConsoleConfig config, Function<String, Actuator> cmdFactory) {
        this.config = config;
        this.cmdFactory = cmdFactory;
    }

    @Override
    protected Actuator findInvoker(String cmdName) {
        return cmdFactory.apply(cmdName);
    }

    @Override
    protected List<String> getInput() {
        return Stream.of(scanner.nextLine())
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .collect(Collectors.toList());
    }

    @Override
    protected void printPrompt() {
        print(grey(this.config.getPrompt()));
    }

    @Override
    protected boolean isExitCmd(String cmdName) {
        for (String cmd : this.config.getExitCmd()) {
            if (cmd == null || cmd.equals(cmdName))
                return true;
        }
        return false;
    }

}
