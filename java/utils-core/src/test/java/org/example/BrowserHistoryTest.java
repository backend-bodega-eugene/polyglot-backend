package org.example;

import com.eugene.utils.BrowserHistory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrowserHistoryTest {

    @Test
    void testBasicNavigation() {
        BrowserHistory browser = new BrowserHistory("home.com");
        assertEquals("home.com", browser.back());

        browser.visit("page1.com");
        assertEquals("page1.com", browser.back());  // no back yet

        browser.visit("page2.com");
        assertEquals("page1.com", browser.back());  // back to page1
        assertEquals("page2.com", browser.forward()); // forward to page2
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
