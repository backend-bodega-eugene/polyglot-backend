package org.example;

import com.eugene.utils.BracketValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BracketValidatorTest {

    @Test
    void testValidBrackets() {
        assertTrue(BracketValidator.isValid("()"));
        assertTrue(BracketValidator.isValid("([]{})"));
        assertTrue(BracketValidator.isValid("{[()()]}"));
        assertTrue(BracketValidator.isValid(""));
    }

    @Test
    void testInvalidBrackets() {
        assertFalse(BracketValidator.isValid("("));
        assertFalse(BracketValidator.isValid("([)]"));
        assertFalse(BracketValidator.isValid("{[}"));
        assertFalse(BracketValidator.isValid("((("));
    }
}
