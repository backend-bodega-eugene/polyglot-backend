package org.example;

import com.eugene.utils.BrowserHistory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrowserHistoryTest {

    @Test
    void testBasicNavigation() {
        BrowserHistory browser = new BrowserHistory("home.com");
        assertEquals("home.com", browser.back()); // backStack 是空的
        browser.visit("page1.com");
        assertEquals("home.com", browser.back()); // 此时才是 page1 ← back// forward to page2
    }

    @Test
    void testForwardClearedAfterVisit() {
        BrowserHistory browser = new BrowserHistory("start.com");
        browser.visit("a.com");
        browser.visit("b.com");
        browser.back();  // now at a.com
        browser.visit("c.com"); // forwardStack should be cleared

        assertEquals("a.com", browser.back());
        assertEquals("c.com", browser.forward());
    }
}
