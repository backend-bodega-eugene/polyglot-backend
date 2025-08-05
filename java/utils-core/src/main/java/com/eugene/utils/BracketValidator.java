package com.eugene.utils;

import java.util.Map;

// 栈 - 括号匹配
public class BracketValidator {

    public static boolean isValid(String s) {
        //我也不知道栈初始值应该填多少,一年12个月就填12吧,网上应该有更靠谱的说法
        //或者应该根据实际的业务
        SynchronizedStack<Character> stack = new SynchronizedStack<>(12);
        Map<Character, Character> map = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (map.containsValue(c)) stack.push(c);
            else if (map.containsKey(c)) {
                if (stack.isEmpty() || stack.pop() != map.get(c)) return false;
            }
        }
        return stack.isEmpty();
    }
}
