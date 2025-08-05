package com.eugene;


import com.eugene.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EugeneApplication {

    public synchronized static void main(String[] args) {

        byte[] plainText = new byte[]{
                0x32, 0x43, (byte) 0xf6, (byte) 0xa8,
                (byte) 0x88, 0x5a, 0x30, (byte) 0x8d,
                0x31, 0x31, (byte) 0x98, (byte) 0xa2,
                (byte) 0xe0, 0x37, 0x07, 0x34
        };

        byte[] key = new byte[]{
                0x2b, 0x7e, 0x15, 0x16,
                0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6,
                (byte) 0xab, (byte) 0xf7, 0x15, (byte) 0x88,
                0x09, (byte) 0xcf, 0x4f, 0x3c
        };

        // 加密
        byte[] encrypted = EugeneAESUtils.aesEncrypt(plainText, key);
        System.out.println("密文:");
        for (byte b : encrypted) {
            System.out.printf("%02X ", b);
        }
        System.out.println();

        // 解密
        byte[] decrypted = UnEugeneAESUtils.aesDecrypt(encrypted, key);
        System.out.println("解密后的明文:");
        for (byte b : decrypted) {
            System.out.printf("%02X ", b);
        }
        System.out.println();

        // 检查是否一致
        boolean isMatch = Arrays.equals(plainText, decrypted);
        System.out.println("解密结果与原文一致: " + isMatch);
        Integer[] nums = {8, 3, 5, 1, 7, 9, 2};

        System.out.println("排序前：");
        QuickSort.printArray(nums);

        QuickSort.quickSort(nums);

        System.out.println("排序后：");
        QuickSort.printArray(nums);
        System.out.println("-----------降序---------------");
        System.out.println("排序前：");
        QuickSort.printArray(nums);

        QuickSort.quickSort(nums,false);

        System.out.println("排序后：");
        QuickSort.printArray(nums);
//        Integer[] arrInteger=new Integer[]{2,4,142,33,4,78,9000,5};
//
//        GenericBubbleSort.bubbleSort(arrInteger);
//        GenericBubbleSort.printArray(arrInteger);
//        GenericBubbleSort.bubbleSort(arrInteger,false);
//        GenericBubbleSort.printArray(arrInteger);
//        int[] arr=new int[]{1,2,4,5,6,7};
//        ArrayUtils.reverse(arr);
//        for(int i=0;i<arr.length;i++){
//            System.out.println(arr[i]);
//        }
    }

}
