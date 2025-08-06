package com.eugene.utils;

/**
 * UnEugeneAESUtils 是一个不依赖 JDK 标准加密库的 AES 解密工具类。
 * 实现了 AES-128 的完整解密逻辑，包括 KeyExpansion、InvSubBytes、InvShiftRows、
 * InvMixColumns、AddRoundKey 等步骤，支持 128 位密钥长度。
 */
public class UnEugeneAESUtils {

    /**
     * 执行 AES 解密操作
     *
     * @param input 被加密的 16 字节数据块
     * @param key   128 位（16 字节）密钥
     * @return 解密后的 16 字节明文数据
     */
    public static byte[] aesDecrypt(byte[] input, byte[] key) {
        byte[][] state = new byte[4][4]; // 状态矩阵
        byte[][] expandedKey = keyExpansion(key); // 扩展密钥，生成 11 轮轮密钥

        // Step 1: 将输入按列写入 4x4 状态矩阵
        for (int i = 0; i < input.length; i++) {
            state[i % 4][i / 4] = input[i];
        }

        // Step 2: 添加第10轮轮密钥（先执行）
        addRoundKey(state, getRoundKey(expandedKey, 10));

        // Step 3: 反向执行 9 轮主循环
        for (int round = 9; round >= 1; round--) {
            invShiftRows(state);                        // 行移位（逆）
            invSubBytes(state);                         // 字节替代（逆）
            addRoundKey(state, getRoundKey(expandedKey, round)); // 添加轮密钥
            invMixColumns(state);                       // 列混淆（逆）
        }

        // Step 4: 最后一轮（无 invMixColumns）
        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, getRoundKey(expandedKey, 0));

        // Step 5: 状态矩阵转为 16 字节数组
        byte[] output = new byte[16];
        for (int i = 0; i < 16; i++) {
            output[i] = state[i % 4][i / 4];
        }

        return output;
    }

    /**
     * 执行列混淆的逆操作（InvMixColumns）
     */
    public static void invMixColumns(byte[][] state) {
        validateStateMatrix(state);
        for (int c = 0; c < 4; c++) {
            byte s0 = state[0][c];
            byte s1 = state[1][c];
            byte s2 = state[2][c];
            byte s3 = state[3][c];
            state[0][c] = (byte) (gmul(s0, 14) ^ gmul(s1, 11) ^ gmul(s2, 13) ^ gmul(s3, 9));
            state[1][c] = (byte) (gmul(s0, 9) ^ gmul(s1, 14) ^ gmul(s2, 11) ^ gmul(s3, 13));
            state[2][c] = (byte) (gmul(s0, 13) ^ gmul(s1, 9) ^ gmul(s2, 14) ^ gmul(s3, 11));
            state[3][c] = (byte) (gmul(s0, 11) ^ gmul(s1, 13) ^ gmul(s2, 9) ^ gmul(s3, 14));
        }
    }

    /**
     * Galois Field (2^8) 中的乘法实现，用于列混淆
     */
    public static byte gmul(byte a, int b) {
        byte result = 0;
        byte aa = a;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                result ^= aa;
            }
            boolean hiBitSet = (aa & 0x80) != 0;
            aa <<= 1;
            if (hiBitSet) {
                aa ^= 0x1b; // 多项式约简
            }
            b >>= 1;
        }
        return result;
    }

    /**
     * 执行密钥扩展（Key Expansion），从 128 位密钥生成 44 个 4 字节字（11 轮密钥）
     */
    private static byte[][] keyExpansion(byte[] key) {
        byte[][] w = new byte[44][4]; // 一共 44 个 Word，每个 Word 是 4 字节

        // 初始化前 4 个 Word
        for (int i = 0; i < 4; i++) {
            w[i][0] = key[4 * i];
            w[i][1] = key[4 * i + 1];
            w[i][2] = key[4 * i + 2];
            w[i][3] = key[4 * i + 3];
        }

        // 生成剩余的 Word
        for (int i = 4; i < 44; i++) {
            byte[] temp = w[i - 1].clone();
            if (i % 4 == 0) {
                temp = subWord(rotWord(temp)); // 字轮转 + S-box 替换
                temp[0] ^= (byte) RCON[(i / 4) - 1];
            }
            for (int j = 0; j < 4; j++) {
                w[i][j] = (byte) (w[i - 4][j] ^ temp[j]);
            }
        }

        return w;
    }

    /**
     * 将某一轮的 key 拆分成 4x4 轮密钥矩阵
     */
    private static byte[][] getRoundKey(byte[][] expandedKey, int round) {
        byte[][] roundKey = new byte[4][4];
        for (int i = 0; i < 4; i++) {
            roundKey[0][i] = expandedKey[round * 4 + i][0];
            roundKey[1][i] = expandedKey[round * 4 + i][1];
            roundKey[2][i] = expandedKey[round * 4 + i][2];
            roundKey[3][i] = expandedKey[round * 4 + i][3];
        }
        return roundKey;
    }

    /**
     * 添加轮密钥（AddRoundKey）
     */
    public static void addRoundKey(byte[][] state, byte[][] roundKey) {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                state[r][c] ^= roundKey[r][c];
            }
        }
    }

    /**
     * 行移位的逆操作（InvShiftRows）
     */
    public static void invShiftRows(byte[][] state) {
        validateStateMatrix(state);
        byte temp;
        // 行1: 右移1位
        temp = state[1][3];
        state[1][3] = state[1][2];
        state[1][2] = state[1][1];
        state[1][1] = state[1][0];
        state[1][0] = temp;

        // 行2: 右移2位
        temp = state[2][0];
        state[2][0] = state[2][2];
        state[2][2] = temp;
        temp = state[2][1];
        state[2][1] = state[2][3];
        state[2][3] = temp;

        // 行3: 右移3位（等效于左移1位）
        temp = state[3][0];
        state[3][0] = state[3][1];
        state[3][1] = state[3][2];
        state[3][2] = state[3][3];
        state[3][3] = temp;
    }

    /**
     * 字节替代的逆操作（InvSubBytes），使用 INV_S_BOX 替换
     */
    public static void invSubBytes(byte[][] state) {
        validateStateMatrix(state);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = INV_S_BOX[state[i][j] & 0xFF];
            }
        }
    }

    /**
     * 执行 RotWord 操作：将 word 左循环移位一位
     * 输入：{0x1a, 0x2b, 0x3c, 0x4d}
     * 输出：{0x2b, 0x3c, 0x4d, 0x1a}
     */
    private static byte[] rotWord(byte[] word) {
        return new byte[]{word[1], word[2], word[3], word[0]};
    }
    /**
     * 校验 state 是否是合法的 4x4 状态矩阵
     * 若结构不合法会抛出 IllegalArgumentException
     */
    private static void validateStateMatrix(byte[][] state) {
        if (state == null || state.length != 4) {
            throw new IllegalArgumentException("AES状态矩阵必须有4行");
        }
        for (int i = 0; i < 4; i++) {
            if (state[i] == null || state[i].length != 4) {
                throw new IllegalArgumentException("AES状态矩阵的每一行必须有4个字节");
            }
        }
    }
    /**
     * 执行 SubWord 操作：对 4 字节 word 中的每个字节使用 S-Box 替换
     */
    private static byte[] subWord(byte[] word) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) S_BOX[word[i] & 0xFF];
        }
        return result;
    }
    /**
     * AES Rcon 常量表，用于 KeyExpansion 的轮常量
     * 10轮，每轮一个字节（仅用于 RotWord 后的第一个字节）
     */
    private static final int[] RCON = {
            0x01, 0x02, 0x04, 0x08,
            0x10, 0x20, 0x40, 0x80,
            0x1B, 0x36
    };
    /**
     * AES 的逆 S-Box，用于 InvSubBytes 步骤（解密专用）
     * 每个元素表示 index 对应值的逆映射
     */
    private static final byte[] INV_S_BOX = {
            (byte)0x52, (byte)0x09, (byte)0x6A, (byte)0xD5, (byte)0x30, (byte)0x36, (byte)0xA5, (byte)0x38,
            (byte)0xBF, (byte)0x40, (byte)0xA3, (byte)0x9E, (byte)0x81, (byte)0xF3, (byte)0xD7, (byte)0xFB,
            (byte)0x7C, (byte)0xE3, (byte)0x39, (byte)0x82, (byte)0x9B, (byte)0x2F, (byte)0xFF, (byte)0x87,
            (byte)0x34, (byte)0x8E, (byte)0x43, (byte)0x44, (byte)0xC4, (byte)0xDE, (byte)0xE9, (byte)0xCB,
            (byte)0x54, (byte)0x7B, (byte)0x94, (byte)0x32, (byte)0xA6, (byte)0xC2, (byte)0x23, (byte)0x3D,
            (byte)0xEE, (byte)0x4C, (byte)0x95, (byte)0x0B, (byte)0x42, (byte)0xFA, (byte)0xC3, (byte)0x4E,
            (byte)0x08, (byte)0x2E, (byte)0xA1, (byte)0x66, (byte)0x28, (byte)0xD9, (byte)0x24, (byte)0xB2,
            (byte)0x76, (byte)0x5B, (byte)0xA2, (byte)0x49, (byte)0x6D, (byte)0x8B, (byte)0xD1, (byte)0x25,
            (byte)0x72, (byte)0xF8, (byte)0xF6, (byte)0x64, (byte)0x86, (byte)0x68, (byte)0x98, (byte)0x16,
            (byte)0xD4, (byte)0xA4, (byte)0x5C, (byte)0xCC, (byte)0x5D, (byte)0x65, (byte)0xB6, (byte)0x92,
            (byte)0x6C, (byte)0x70, (byte)0x48, (byte)0x50, (byte)0xFD, (byte)0xED, (byte)0xB9, (byte)0xDA,
            (byte)0x5E, (byte)0x15, (byte)0x46, (byte)0x57, (byte)0xA7, (byte)0x8D, (byte)0x9D, (byte)0x84,
            (byte)0x90, (byte)0xD8, (byte)0xAB, (byte)0x00, (byte)0x8C, (byte)0xBC, (byte)0xD3, (byte)0x0A,
            (byte)0xF7, (byte)0xE4, (byte)0x58, (byte)0x05, (byte)0xB8, (byte)0xB3, (byte)0x45, (byte)0x06,
            (byte)0xD0, (byte)0x2C, (byte)0x1E, (byte)0x8F, (byte)0xCA, (byte)0x3F, (byte)0x0F, (byte)0x02,
            (byte)0xC1, (byte)0xAF, (byte)0xBD, (byte)0x03, (byte)0x01, (byte)0x13, (byte)0x8A, (byte)0x6B,
            (byte)0x3A, (byte)0x91, (byte)0x11, (byte)0x41, (byte)0x4F, (byte)0x67, (byte)0xDC, (byte)0xEA,
            (byte)0x97, (byte)0xF2, (byte)0xCF, (byte)0xCE, (byte)0xF0, (byte)0xB4, (byte)0xE6, (byte)0x73,
            (byte)0x96, (byte)0xAC, (byte)0x74, (byte)0x22, (byte)0xE7, (byte)0xAD, (byte)0x35, (byte)0x85,
            (byte)0xE2, (byte)0xF9, (byte)0x37, (byte)0xE8, (byte)0x1C, (byte)0x75, (byte)0xDF, (byte)0x6E,
            (byte)0x47, (byte)0xF1, (byte)0x1A, (byte)0x71, (byte)0x1D, (byte)0x29, (byte)0xC5, (byte)0x89,
            (byte)0x6F, (byte)0xB7, (byte)0x62, (byte)0x0E, (byte)0xAA, (byte)0x18, (byte)0xBE, (byte)0x1B,
            (byte)0xFC, (byte)0x56, (byte)0x3E, (byte)0x4B, (byte)0xC6, (byte)0xD2, (byte)0x79, (byte)0x20,
            (byte)0x9A, (byte)0xDB, (byte)0xC0, (byte)0xFE, (byte)0x78, (byte)0xCD, (byte)0x5A, (byte)0xF4,
            (byte)0x1F, (byte)0xDD, (byte)0xA8, (byte)0x33, (byte)0x88, (byte)0x07, (byte)0xC7, (byte)0x31,
            (byte)0xB1, (byte)0x12, (byte)0x10, (byte)0x59, (byte)0x27, (byte)0x80, (byte)0xEC, (byte)0x5F,
            (byte)0x60, (byte)0x51, (byte)0x7F, (byte)0xA9, (byte)0x19, (byte)0xB5, (byte)0x4A, (byte)0x0D,
            (byte)0x2D, (byte)0xE5, (byte)0x7A, (byte)0x9F, (byte)0x93, (byte)0xC9, (byte)0x9C, (byte)0xEF,
            (byte)0xA0, (byte)0xE0, (byte)0x3B, (byte)0x4D, (byte)0xAE, (byte)0x2A, (byte)0xF5, (byte)0xB0,
            (byte)0xC8, (byte)0xEB, (byte)0xBB, (byte)0x3C, (byte)0x83, (byte)0x53, (byte)0x99, (byte)0x61,
            (byte)0x17, (byte)0x2B, (byte)0x04, (byte)0x7E, (byte)0xBA, (byte)0x77, (byte)0xD6, (byte)0x26,
            (byte)0xE1, (byte)0x69, (byte)0x14, (byte)0x63, (byte)0x55, (byte)0x21, (byte)0x0C, (byte)0x7D
    };
    /**
     * AES 的 S-Box，用于 SubBytes 步骤（加密/密钥扩展时使用）
     */
    private static final int[] S_BOX = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5,
            0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0,
            0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc,
            0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a,
            0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0,
            0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b,
            0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85,
            0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5,
            0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17,
            0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88,
            0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c,
            0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9,
            0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6,
            0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e,
            0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94,
            0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68,
            0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };
}
