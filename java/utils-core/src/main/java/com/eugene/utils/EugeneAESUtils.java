package com.eugene.utils;

/**
 * AES加密工具类：Eugene版本
 * <p>
 * 支持 128 位密钥、16字节明文块的 AES 标准加密过程。
 * 实现步骤：SubBytes → ShiftRows → MixColumns → AddRoundKey
 * 共 10轮，第0轮仅执行 AddRoundKey，最后一轮不执行 MixColumns
 * <p>
 * 注意：本工具类不依赖于 javax.crypto 标准库，整体加密逻辑由编程者手动实现
 */
public class EugeneAESUtils {

    /**
     * AES加密方法
     *
     * @param input 明文，长度为16字节
     * @param key   密钥，长度为16字节
     * @return 加密后的密文，长度为16字节
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key) {
        byte[][] state = new byte[4][4];
        byte[][] expandedKey = keyExpansion(key);

        // 将输入填入 state 矩阵，按列排列
        for (int i = 0; i < 16; i++) {
            state[i % 4][i / 4] = input[i];
        }

        // 初始轮：仅执行 AddRoundKey
        addRoundKey(state, getRoundKey(expandedKey, 0));

        // 中间9轮
        for (int round = 1; round < 10; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state);
            addRoundKey(state, getRoundKey(expandedKey, round));
        }

        // 最后一轮不执行 MixColumns
        subBytes(state);
        shiftRows(state);
        addRoundKey(state, getRoundKey(expandedKey, 10));

        // 将 state 导出为字节数组
        byte[] output = new byte[16];
        for (int i = 0; i < 16; i++) {
            output[i] = state[i % 4][i / 4];
        }

        return output;
    }

    /**
     * 获取某轮的子密钥（从扩展密钥中提取）
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
     * SubBytes 步骤：将 state 中的每个字节替换为 S_BOX 中对应的值
     */
    public static void subBytes(byte[][] state) {
        validateStateMatrix(state);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int value = state[row][col] & 0xFF;
                state[row][col] = (byte) S_BOX[value];
            }
        }
    }

    /**
     * ShiftRows 步骤：每一行循环左移 row 位
     */
    public static void shiftRows(byte[][] state) {
        validateStateMatrix(state);
        for (int row = 1; row < 4; row++) {
            byte[] temp = new byte[4];
            for (int col = 0; col < 4; col++) {
                temp[col] = state[row][(col + row) % 4];
            }
            for (int col = 0; col < 4; col++) {
                state[row][col] = temp[col];
            }
        }
    }

    /**
     * AddRoundKey 步骤：每个字节与子密钥对应字节异或
     */
    public static void addRoundKey(byte[][] state, byte[][] roundKey) {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                state[r][c] ^= roundKey[r][c];
            }
        }
    }

    /**
     * MixColumns 步骤：每一列进行 Galois Field 上的混合
     */
    public static void mixColumns(byte[][] state) {
        for (int c = 0; c < 4; c++) {
            byte[] column = new byte[4];
            for (int r = 0; r < 4; r++) {
                column[r] = state[r][c];
            }
            byte[] mixed = mixSingleColumn(column);
            for (int r = 0; r < 4; r++) {
                state[r][c] = mixed[r];
            }
        }
    }

    /**
     * 对单列进行 MixColumns 操作
     */
    private static byte[] mixSingleColumn(byte[] column) {
        byte[] result = new byte[4];
        result[0] = (byte) (gmul(column[0], (byte) 2) ^ gmul(column[1], (byte) 3) ^ column[2] ^ column[3]);
        result[1] = (byte) (column[0] ^ gmul(column[1], (byte) 2) ^ gmul(column[2], (byte) 3) ^ column[3]);
        result[2] = (byte) (column[0] ^ column[1] ^ gmul(column[2], (byte) 2) ^ gmul(column[3], (byte) 3));
        result[3] = (byte) (gmul(column[0], (byte) 3) ^ column[1] ^ column[2] ^ gmul(column[3], (byte) 2));
        return result;
    }

    /**
     * Galois域上的乘法（GF(2^8)）
     */
    private static byte gmul(byte a, byte b) {
        byte p = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            boolean highBitSet = (a & 0x80) != 0;
            a <<= 1;
            if (highBitSet) {
                a ^= 0x1B; // Rijndael 多项式
            }
            b >>= 1;
        }
        return p;
    }

    /**
     * 验证 state 是否为 4x4 矩阵
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
     * 密钥扩展：将 128 位密钥扩展为 11 个轮密钥（共44组word）
     */
    private static byte[][] keyExpansion(byte[] key) {
        byte[][] w = new byte[44][4];

        // 初始化前4组
        for (int i = 0; i < 4; i++) {
            w[i][0] = key[4 * i];
            w[i][1] = key[4 * i + 1];
            w[i][2] = key[4 * i + 2];
            w[i][3] = key[4 * i + 3];
        }

        // 扩展剩下的40组
        for (int i = 4; i < 44; i++) {
            byte[] temp = w[i - 1].clone();
            if (i % 4 == 0) {
                temp = subWord(rotWord(temp));
                temp[0] ^= (byte) RCON[(i / 4) - 1];
            }
            for (int j = 0; j < 4; j++) {
                w[i][j] = (byte) (w[i - 4][j] ^ temp[j]);
            }
        }

        return w;
    }

    /**
     * RotWord：字节循环左移1位
     */
    private static byte[] rotWord(byte[] word) {
        return new byte[]{word[1], word[2], word[3], word[0]};
    }

    /**
     * SubWord：将4字节word中每个字节进行S-Box替换
     */
    private static byte[] subWord(byte[] word) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) S_BOX[word[i] & 0xFF];
        }
        return result;
    }
    /**
     * S-Box：字节替换表（AES 标准的固定表）
     */
    private static final int[] S_BOX = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };

    /**
     * Rijndael Rcon 表：用于密钥扩展过程中的常量轮值
     */
    private static final int[] RCON = {
            0x01, 0x02, 0x04, 0x08,
            0x10, 0x20, 0x40, 0x80,
            0x1B, 0x36
    };

    /**
     * 示例用法：加密一组16字节明文
     */
    public static void main(String[] args) {
        byte[] plainText = new byte[]{
                (byte) 0x32, (byte) 0x43, (byte) 0xf6, (byte) 0xa8,
                (byte) 0x88, (byte) 0x5a, (byte) 0x30, (byte) 0x8d,
                (byte) 0x31, (byte) 0x31, (byte) 0x98, (byte) 0xa2,
                (byte) 0xe0, (byte) 0x37, (byte) 0x07, (byte) 0x34
        };

        byte[] key = new byte[]{
                (byte) 0x2b, (byte) 0x7e, (byte) 0x15, (byte) 0x16,
                (byte) 0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6,
                (byte) 0xab, (byte) 0xf7, (byte) 0x15, (byte) 0x88,
                (byte) 0x09, (byte) 0xcf, (byte) 0x4f, (byte) 0x3c
        };

        byte[] encrypted = aesEncrypt(plainText, key);
        System.out.println("密文:");
        for (byte b : encrypted) {
            System.out.printf("%02X ", b);
        }
    }
    }