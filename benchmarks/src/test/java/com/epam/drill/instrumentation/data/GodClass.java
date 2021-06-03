package com.epam.drill.instrumentation.data;

public class GodClass implements Runnable {

    @Override
    public void run() {
        covered("hf", -32);
        covered("hf", 23);
        covered("4", 0);
        bubbleSort(new int[]{5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 1});
        bubbleSort(new int[]{5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 115, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 115, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 115, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 1});
        printArray(new int[]{5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 5, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 15, 7, 2, 3, 1});
        sumAll(1, 2, 3, 4, 5);
        fuckingIncrement(31);
        chooseString(2);
    }

    void bubbleSort(int arr[]) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr[j] > arr[j + 1]) {
                    // swap arr[j+1] and arr[j]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
    }

    void printArray(int arr[]) {
        int n = arr.length;
        for (int i = 0; i < n; ++i)
            System.out.print(arr[i] + " ");
        System.out.println();
    }

    public int sumAll(int... args) {

        int sum = 0;

        for (int arg : args) {
            sum += arg;
        }

        return sum;
    }

    public int fuckingIncrement(int value) {
        int initialValue = value;
        int someValue = 10000;

        while (value != 1000) {
            value++;
        }

        for (int i = 100; i > 0; i--) {
            value--;
        }

        for (int i = 0; i < 2340; i++) {
            for (int j = 0; j < 14564; j++) {
                value++;
                covered("31", j);
                while (value != 0) {
                    value--;
                    covered("31", i);
                    if (value % 2 == 0) {
                        someValue = value;
                    }
                }
            }
        }
        value = someValue;
        System.out.println(value);

        return initialValue + 1;
    }

    public String chooseString(int index) {
        switch (index) {
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3";
            default:
                return "default";
        }
    }

    private String covered(String s, int c) {
        String z;
        if (c > 0) {
            z = s + c;
        } else if (c == 0) {
            z = s + c + 31;
        } else {
            z = "gf" + c;
        }
        return z;
    }


}
