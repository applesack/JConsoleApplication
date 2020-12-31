package xyz.scootaloo.console.app.support.component;

import xyz.scootaloo.console.app.support.common.Colorful;

import java.util.Scanner;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:42
 */
public interface ResourceManager {

    Scanner scanner = new Scanner(System.in);
    Colorful cPrint = Colorful.instance;

}
