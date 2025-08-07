package com.eugene.utils;

import java.util.Map;

/**
 * 工具类：括号匹配校验器
 * <p>
 * 支持三种括号类型：()、[]、{} 的成对匹配校验
 * </p>
 * 示例：
 * <ul>
 *     <li>输入："{[()]}" 返回 true</li>
 *     <li>输入："({[)]}" 返回 false</li>
 * </ul>
 *
 * 使用线程安全的 {@link SynchronizedStack} 进行栈操作
 * @author Eugene
 */
public class BracketValidator {

    /**
     * 判断一个字符串中的括号是否成对匹配
     *
     * @param s 输入字符串，可能包含 (), [], {}
     * @return 若括号匹配成功返回 true，否则返回 false
     */
    public static boolean isValid(String s) {
        // 初始栈容量设为12 —— 参考了“1年12个月”的灵感😏
        // 实际中可以根据字符串长度或业务复杂度动态调整
        SynchronizedStack<Character> stack = new SynchronizedStack<>(12);

        // 映射：右括号 -> 左括号，用于匹配校验
        Map<Character, Character> map = Map.of(
                ')', '(',
                ']', '[',
                '}', '{'
        );

        // 遍历字符串中的每个字符
        for (char c : s.toCharArray()) {
            // 如果是左括号，入栈
            if (map.containsValue(c)) {
                stack.push(c);
            }
            // 如果是右括号，尝试出栈并匹配
            else if (map.containsKey(c)) {
                // 栈为空 或 弹出值不匹配 => 非法
                if (stack.isEmpty() || stack.pop() != map.get(c)) {
                    return false;
                }
            }
            // 非括号字符忽略（也可以扩展为非法处理）
        }

        // 最终栈必须为空，说明全部括号都匹配完毕
        return stack.isEmpty();
    }
}
