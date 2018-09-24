package com.company;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Optional;
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
    private String errorString;
    private String computationTimeString;

    public String getErrorString() {
        return errorString;
    }

    public String getComputationTimeString() {
        return computationTimeString;
    }

    public Optional<Integer> computeFunctions(Function<Integer, Integer> f, Function<Integer, Integer> g, int arg1, int arg2) {

        errorString = "";
        computationTimeString = "";

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
            return Optional.empty();
        }
        Runnable fR = createRunnableFromFunc(f, arg1, outputStreams[0]);
        Runnable gR = createRunnableFromFunc(g, arg2, outputStreams[1]);

        Thread[] threads = {
                new Thread(fR),
                new Thread(gR)
        };

        for (int i = 0; i < 2; i++)
            threads[i].start();

        ArrayList<Optional<Integer>> vals = new ArrayList<>();
        vals.add(Optional.empty());
        vals.add(Optional.empty());

        Optional<Integer> result = Optional.empty();
        try {
            long timerStart = System.nanoTime(), timer = 0;

            long computationTime = 0, computationStart = System.nanoTime();
            boolean promptEnabled = true;
            boolean resultObtained = false;
            boolean terminate = false;
            while (!resultObtained) {
                long start = System.nanoTime();
                for (int i = 0; i < 2; i++) {
                    if (!vals.get(i).isPresent() && inputStreams[i].available() >= 4) {

                        vals.set(i, Optional.of(IOOperations.getInt(inputStreams[i])));
                        if (vals.get(i).get().equals(BinaryOperation.getZeroValue())) {
                            result = Optional.of(BinaryOperation.getZeroResult());
                            resultObtained = true;
                            break;
                        } else if (vals.get(0).isPresent() && vals.get(1).isPresent()) {
                            result = Optional.of(BinaryOperation.calculate(vals.get(0).get(), vals.get(1).get()));
                            resultObtained = true;
                            break;
                        }
                    }
                }

                if (terminate || resultObtained)
                    break;

                Thread.sleep(100);

                timer = System.nanoTime() - timerStart;

                if (promptEnabled && timer / 1_000_000_000 >= 5) {
                    System.out.println("(C)ontinue, Continue (W)ithout prompt, (T)erminate:");
                    String choice;
                    do {
                        Scanner input = new Scanner(System.in);
                        choice = input.next().toLowerCase();
                    } while (choice.equals("c") && choice.equals("w") && choice.equals("t"));
                    if (choice.equals("w")) {
                        promptEnabled = false;
                    } else if (choice.equals("t")) {
                        terminate = true;
                    }
                    timer = System.nanoTime();
                }

            }
            for (int i = 0; i < 2; i++) {
                threads[i].interrupt();
                inputStreams[i].close();
                outputStreams[i].close();
            }
            if (terminate && !result.isPresent()) {
                boolean comma = false;
                if (!vals.get(0).isPresent()) {
                    errorString += "f was not computed";
                    comma = true;
                }
                if (!vals.get(1).isPresent()) {
                    if (comma)
                        errorString += ", ";
                    errorString += "g was not computed";
                }
            }
            computationTime = System.nanoTime() - computationStart;
            computationTimeString = String.format(Main.ANSI_YELLOW + "Computation took %f s\n" +  Main.ANSI_RESET, (double)computationTime / 1_000_000_000);
            return result;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
