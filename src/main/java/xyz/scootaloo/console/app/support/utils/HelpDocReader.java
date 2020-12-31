package xyz.scootaloo.console.app.support.utils;

import java.io.File;
import java.net.URL;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/31 11:59
 */
public class HelpDocReader {

    private static final String helpFile = "/help.txt";

    private static void load() {
        File helpDoc = getFile();

    }

    private static File getFile() {
        URL helpFileUrl =  HelpDocReader.class.getResource(helpFile);
        if (helpFileUrl == null)
            throw new RuntimeException("未找到help.txt文件，help命令将不可用");
        return new File(helpFileUrl.getFile());
    }

}
