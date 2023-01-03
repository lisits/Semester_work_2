package ownservlet;

import java.io.IOException;
import java.io.InputStream;

public class MessageInputStream extends InputStream {
    private InputStream inputStream;
    public byte firstByte;
    public byte secondByte;
    private static boolean[] vector;

    public MessageInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    // the first 2 bytes are a digit before the dot in the protocol version and a digit after,
    // to check the correctness of the message
    // 3 byte - type
    // 4, 5 - body length
    public Message readMessage() throws IOException {
        int firstByte = inputStream.read();

        if (firstByte == -1){
            return null;
        }

        byte firstInputByte = (byte) firstByte;
        byte secondInputByte = (byte) inputStream.read();

        if(firstInputByte != firstByte || secondInputByte != secondByte){
            throw new IllegalArgumentException();
        }

        byte type = (byte) inputStream.read();

        int length = inputStream.read()<<8 | inputStream.read();

        byte[] data = new byte[length];

        for(int i = 0; i < length; i++){
            data[i] = (byte) inputStream.read();
        }

        return new Message(type, data);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }


    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    public int read() throws IOException {
        return inputStream.read();
    }
}

