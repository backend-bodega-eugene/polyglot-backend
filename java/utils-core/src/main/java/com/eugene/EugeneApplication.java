package com.eugene;


import com.eugene.utils.ArrayUtils;
import com.eugene.utils.GenericBubbleSort;
import com.eugene.utils.QuickSort;

import java.util.ArrayList;
import java.util.List;

public class EugeneApplication {

    public static void main(String[] args) {
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
