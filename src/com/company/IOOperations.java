package com.company;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

public class IOOperations {
    static void putInt(PipedOutputStream output, int i) throws IOException {
        ByteBuffer result = ByteBuffer.allocate(4);
        result.putInt(i);
        output.write(result.array(), 0, 4);
    }

    static int getInt(PipedInputStream input) throws IOException {
        if (input.available() < 4)
            throw new IOException("Not enough bytes");
        byte[] bytes = new byte[4];

        int i = input.read(bytes, 0, 4);
        ByteBuffer resBuffer = ByteBuffer.wrap(bytes);
        return resBuffer.getInt();
    }
}
