package xyz.scootaloo.console.app.support.utils;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ResourceManager;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * todo
 * @author flutterdash@qq.com
 * @since 2020/12/31 11:59
 */
@Deprecated
public abstract class HelpDocReader {

    private static final String helpFile = "/help.txt";
    private static final Colorful cPrint = ResourceManager.cPrint;
    private static final Map<String, HelpDoc> docMap = new HashMap<>();

    private static void load() {
        File helpDocFile = getFile();
        readAndLoad(helpDocFile);
    }

    private static void readAndLoad(File helpDocFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(helpDocFile))) {
            String line;
            boolean isOpen = false;
            List<String> cmdList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                if (line.startsWith("[") && line.endsWith("]")) {
                    if (isOpen) {
                        cmdList.clear();

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getFile() {
        URL helpFileUrl =  HelpDocReader.class.getResource(helpFile);
        if (helpFileUrl == null)
            throw new RuntimeException("未找到help.txt文件，help命令将不可用");
        return new File(helpFileUrl.getFile());
    }

    private static class HelpDoc {

        private final List<String> cmdNames;
        private List<String> contents;

        public HelpDoc(List<String> cmdNames) {
            this.cmdNames = cmdNames;
        }

        private void setContents(List<String> contents) {
            this.contents = contents;
        }

        public void printHelpInf() {
            for (String line : contents)
                cPrint.println(line);
        }

        public static void printHead(String cmd) {
            cPrint.println("[" + cmd + "]");
        }

    }

}
