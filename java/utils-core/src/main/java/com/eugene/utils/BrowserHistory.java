package com.eugene.utils;

// 浏览器后退功能模拟
public class BrowserHistory {
    private final SynchronizedStack<String> backStack = new SynchronizedStack<>(12);
    private final SynchronizedStack<String> forwardStack = new SynchronizedStack<>(12);
    private String currentPage;

    public BrowserHistory(String homepage) {
        currentPage = homepage;
    }

    public void visit(String url) {
        backStack.push(currentPage);
        currentPage = url;
        forwardStack.clear();
    }

    public String back() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentPage);
            currentPage = backStack.pop();
        }
        return currentPage;
    }

    public String forward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentPage);
            currentPage = forwardStack.pop();
        }
        return currentPage;
    }
}
