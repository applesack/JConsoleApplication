package xyz.scootaloo.console.app.support.component;

import xyz.scootaloo.console.app.support.common.Colorful;

import java.util.Scanner;

/**
 * 资源管理，管理一些常用可通用的资源，例如单例
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:42
 */
public interface ResourceManager {

    Scanner scanner = new Scanner(System.in);
    Colorful cPrint = Colorful.instance;

}
