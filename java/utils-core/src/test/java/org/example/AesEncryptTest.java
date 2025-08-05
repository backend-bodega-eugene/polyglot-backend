package org.example;

import com.eugene.utils.EugeneAESUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AesEncryptTest {

    @Test
    void testAesEncrypt_LengthCorrect() {
        byte[] input = new byte[16]; // 全0明文
        byte[] key = new byte[16];   // 全0密钥

        byte[] output = EugeneAESUtils.aesEncrypt(input, key);
        assertNotNull(output);
        assertEquals(16, output.length);
    }

    @Test
    void testAesEncrypt_DifferentInputProducesDifferentOutput() {
        byte[] key = new byte[16];
        byte[] input1 = new byte[16];
        byte[] input2 = new byte[16];
        input2[0] = 1;

        byte[] output1 = EugeneAESUtils.aesEncrypt(input1, key);
        byte[] output2 = EugeneAESUtils.aesEncrypt(input2, key);

        assertNotEquals(new String(output1), new String(output2));
    }

    @Test
    void testAesEncrypt_KeySensitivity() {
        byte[] input = new byte[16];
        byte[] key1 = new byte[16];
        byte[] key2 = new byte[16];
        key2[0] = 1;

        byte[] output1 = EugeneAESUtils.aesEncrypt(input, key1);
        byte[] output2 = EugeneAESUtils.aesEncrypt(input, key2);

        assertNotEquals(new String(output1), new String(output2));
    }
}
