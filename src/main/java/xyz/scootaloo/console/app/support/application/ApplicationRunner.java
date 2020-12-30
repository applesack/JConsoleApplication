package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.config.ConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.Actuator;
import xyz.scootaloo.console.app.support.parser.AssemblyFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ApplicationRunner {

    public static void run(ConfigProvider provider) {
        Objects.requireNonNull(provider);
        ConsoleConfig config = provider.getConfig();
        switch (config.getAppType()) {
            case Standard: {
                standard(config, AssemblyFactory::findInvoker).run();
            } break;
            case Client:
            case Server: {
                System.out.println("目前系统还不支持其他应用类型，请将AppType修改为[AppType.Standard]");
            }
        }
    }

    private static AbstractApplication standard(ConsoleConfig config, Function<String, Actuator> finder) {
        AssemblyFactory.init(config);
        return new ConsoleApplication(config, finder);
    }

}
