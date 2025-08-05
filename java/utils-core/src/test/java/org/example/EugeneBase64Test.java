package org.example;

import com.eugene.utils.EugeneBase64;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class EugeneBase64Test {

    @Test
    void testEncodeDecodeSymmetry() {
        String input = "hello,Eugene";
        String encoded = EugeneBase64.myBase64Encode(input);
        String decoded = EugeneBase64.myBase64DecodeToString(encoded);
        assertEquals(input, decoded);
    }

    @Test
    void testEmptyString() {
        assertEquals("", EugeneBase64.myBase64Encode(""));
        assertEquals("", EugeneBase64.myBase64DecodeToString(""));
    }

    @Test
    void testNonAsciiCharacters() {
        String chinese = "你好，老胡";
        String encoded = EugeneBase64.myBase64Encode(chinese);
        String decoded = EugeneBase64.myBase64DecodeToString(encoded);
        assertEquals(chinese, decoded);
    }

    @Test
    void testDifferentLengths() {
        assertEquals("YQ==", EugeneBase64.myBase64Encode("a"));
        assertEquals("YWI=", EugeneBase64.myBase64Encode("ab"));
        assertEquals("YWJj", EugeneBase64.myBase64Encode("abc"));
        assertEquals("YWJjZA==", EugeneBase64.myBase64Encode("abcd"));
        assertEquals("YWJjZGU=", EugeneBase64.myBase64Encode("abcde"));
    }
}
