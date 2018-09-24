package com.company;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static void main(String[] args) {
        // write your code here


        Function<Integer, Integer> f = (integer) -> {
            try {
                if (integer == -1) {
                    while (true) {
                        Thread.sleep(100);
                        if (5<4)
                            break;
                    }
                    return 8;
                }
                Thread.sleep(500*(integer+1));
                return integer * 3;
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return -1;
        };
        Function<Integer, Integer> g = (integer) -> {
            try {
                if (integer == -1) {
                    while (true) {
                        Thread.sleep(100);
                        if (5<4)
                            break;
                    }
                    return -1;
                }
                Thread.sleep(500*(integer+1));
                return integer;


            } catch(InterruptedException e) {
               // e.printStackTrace();
                Thread.currentThread().interrupt();

            }
            return -1;
        };

        int argsF[] = {1, 2, 0, -1, 3, -1, 10};
        int argsG[] = {2, 1, -1, 0, -1, 2, 9};
        // delayF = {1000, 1500, 500, INF, 2000, INF, 5500}
        // delayG = {1500, 100, INF, 500, INF, 1500, 5000}
        // resF = {1, 2, 0, no, 3, no, 10}
        // resG = {2, 1, no, 0, no, 2, 9};
        for (int i = 0; i < argsF.length; i++) {
            System.out.printf(ANSI_GREEN + "Test #%d:\n" + ANSI_RESET, i+1);
            int result = MultiThreadedComputation.computeFunctions(f, g, argsF[i], argsG[i]);
            if (result != -1)
                System.out.printf("The result of calculation is: %d\n", result);
            else
                System.out.println("The operation canceled by user");
        }

    }
}