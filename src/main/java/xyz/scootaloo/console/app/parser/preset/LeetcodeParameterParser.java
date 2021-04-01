package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.parser.MethodMeta;
import xyz.scootaloo.console.app.parser.NameableParameterParser;
import xyz.scootaloo.console.app.parser.ResultWrapper;

/**
 * leetcode
 * 实验性功能
 *
 * @author flutterdash@qq.com
 * @since 2021/3/31 13:28
 */
public class LeetcodeParameterParser implements NameableParameterParser {
    protected static LeetcodeParameterParser INSATNCE = new LeetcodeParameterParser();

    @Override
    public String name() {
        return "leetcode";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) throws Exception {
        return null;
    }

}
