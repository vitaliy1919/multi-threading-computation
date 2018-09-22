package com.company;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;
import java.util.function.Function;

public class MultiThreadedComputation {
    private static Runnable createRunnableFromFunc(Function<Integer, Integer> f, int arg1, PipedOutputStream out) {
        return new Runnable() {
            @Override
            public void run() {
                try {

                    int result = f.apply(arg1);
                    IOOperations.putInt(out, result);

                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    } else {
                        //e.printStackTrace();
                    }
                }
            }
        };
    }
    static public int computeFunctions(Function<Integer, Integer> f, Function<Integer, Integer> g, int arg1, int arg2) {

        PipedInputStream[] inputStreams = new PipedInputStream[]{
                new PipedInputStream(), new PipedInputStream()
        };
        PipedOutputStream[] outputStreams = new PipedOutputStream[]{
                new PipedOutputStream(), new PipedOutputStream()
        };


        try {
            for (int i = 0; i < 2; i++) {
                inputStreams[i].connect(outputStreams[i]);
            }
        } catch (IOException e) {
            System.out.println("Error");
            return -1;
        }
        Runnable fR = createRunnableFromFunc(f, arg1, outputStreams[0]);
        Runnable gR = createRunnableFromFunc(g, arg2, outputStreams[1]);

        Thread[] threads = {
                new Thread(fR),
                new Thread(gR)
        };

        for (int i = 0; i < 2; i++)
            threads[i].start();

        int[] vals = {
                -1, -1
        };
        int result = -1;
        try {
            long timer = 0;
            long computation_time = 0;
            boolean prompt_enabled = true;
            boolean result_obtained = false;
            boolean terminate = false;
            while (!result_obtained) {
                long start = System.nanoTime();
                for (int i = 0; i < 2; i++) {
                    if (inputStreams[i].available() >= 4 && vals[i] == -1) {

                        vals[i] = IOOperations.getInt(inputStreams[i]);
                        System.out.printf("%d: %d\n", i + 1, vals[i]);
                        if (vals[i] == BinaryOperation.getZeroValue()) {
                            result = BinaryOperation.getZeroResult();
                            result_obtained = true;
                        } else if (vals[0] != -1 && vals[1] != -1) {
                            result = BinaryOperation.calculate(vals[0], vals[1]);
                            result_obtained = true;
                        }
                    }
                }

                if (terminate || result_obtained)
                    break;
                Thread.sleep(100);

                long time_lapsed = System.nanoTime() - start;
                timer += time_lapsed;

                if (prompt_enabled && timer / 1_000_000_000 >= 5) {
                    System.out.println("(C)ontinue, Continue (W)ithout prompt, (T)erminate:");
                    String choice;
                    do {
                        Scanner input = new Scanner(System.in);
                        choice = input.next().toLowerCase();
                    } while (choice.equals("c") && choice.equals("w") && choice.equals("t"));
                    if (choice.equals("w")) {
                        prompt_enabled = false;
                    } else if (choice.equals("t")) {
                        //tresult = -1;
                        terminate = true;
                    }
                    timer = 0;
                }
                computation_time += System.nanoTime() - start;

            }


            for (int i = 0; i < 2; i++) {
                threads[i].interrupt();
                inputStreams[i].close();
                outputStreams[i].close();
            }
            System.out.printf(Main.ANSI_YELLOW + "Computation took %f s\n" +  Main.ANSI_RESET, (double)computation_time / 1_000_000_000);
            return result;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
