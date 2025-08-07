package com.eugene.utils;

/**
 * 浏览器历史记录模拟器
 * <p>
 * 模拟浏览器的后退与前进功能，支持 visit、back、forward 三种操作。
 * 使用两个栈结构分别保存“后退栈”和“前进栈”。
 * </p>
 *
 * 示例用法：
 * <pre>{@code
 * BrowserHistory browser = new BrowserHistory("首页");
 * browser.visit("百度");
 * browser.back();       // 返回“首页”
 * browser.forward();    // 返回“百度”
 * }</pre>
 *
 * 栈由线程安全的 {@link SynchronizedStack} 实现
 * @author Eugene
 */
public class BrowserHistory {

    /** 后退栈：用于记录访问过的页面（可返回） */
    private final SynchronizedStack<String> backStack = new SynchronizedStack<>(12);

    /** 前进栈：用于记录回退后可前进的页面 */
    private final SynchronizedStack<String> forwardStack = new SynchronizedStack<>(12);

    /** 当前正在浏览的页面 */
    private String currentPage;

    /**
     * 构造函数：初始化首页
     *
     * @param homepage 起始页（例如："https://eugene.com"）
     */
    public BrowserHistory(String homepage) {
        currentPage = homepage;
    }

    /**
     * 访问新页面：将当前页压入后退栈，并清空前进栈
     *
     * @param url 新访问的页面 URL
     */
    public void visit(String url) {
        backStack.push(currentPage);   // 当前页入后退栈
        currentPage = url;             // 切换当前页
        forwardStack.clear();          // 清空前进栈（因为断开了前进路径）
    }

    /**
     * 执行后退操作
     * <p>
     * 若后退栈不为空：将当前页压入前进栈，弹出后退页作为当前页
     * </p>
     *
     * @return 当前页面（可能是后退后的页面）
     */
    public String back() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentPage);   // 当前页入前进栈
            currentPage = backStack.pop();    // 后退一页
        }
        return currentPage;
    }

    /**
     * 执行前进操作
     * <p>
     * 若前进栈不为空：将当前页压入后退栈，弹出前进页作为当前页
     * </p>
     *
     * @return 当前页面（可能是前进后的页面）
     */
    public String forward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentPage);      // 当前页入后退栈
            currentPage = forwardStack.pop(); // 前进一页
        }
        return currentPage;
    }
}
