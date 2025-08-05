package org.example;

import com.eugene.utils.EugeneAESUtils;
import com.eugene.utils.UnEugeneAESUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnEugeneAESUtilsTest {

    @Test
    void testEncryptDecryptSymmetry() {
        byte[] key = new byte[16]; // 全 0
        byte[] data = "hello,Eugene!".getBytes();
        byte[] padded = new byte[16];
        System.arraycopy(data, 0, padded, 0, data.length);

        byte[] encrypted = EugeneAESUtils.aesEncrypt(padded, key);
        byte[] decrypted = UnEugeneAESUtils.aesDecrypt(encrypted, key);

        assertArrayEquals(padded, decrypted);
    }

    @Test
    void testDecryptWithWrongKeyFails() {
        byte[] key1 = new byte[16];
        byte[] key2 = new byte[16];
        key2[0] = 1;

        byte[] data = "1234567890123456".getBytes();
        byte[] encrypted = EugeneAESUtils.aesEncrypt(data, key1);
        byte[] decrypted = UnEugeneAESUtils.aesDecrypt(encrypted, key2);

        assertNotEquals(new String(data), new String(decrypted));
    }

    @Test
    void testEncryptDecryptAllZeros() {
        byte[] key = new byte[16];
        byte[] input = new byte[16]; // 全0明文
        byte[] encrypted = EugeneAESUtils.aesEncrypt(input, key);
        byte[] decrypted = UnEugeneAESUtils.aesDecrypt(encrypted, key);
        assertArrayEquals(input, decrypted);
    }
}
