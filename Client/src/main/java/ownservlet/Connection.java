package ownservlet;

import lombok.Data;

import java.io.IOException;
import java.net.Socket;

@Data
public class Connection {
    private Socket socket;
    private MessageInputStream inputStream;
    private MessageOutputStream outputStream;

    public Connection(String address, int port) {
        try {
            this.socket = new Socket(address, port);
            outputStream = new MessageOutputStream(socket.getOutputStream());
            inputStream = new MessageInputStream(socket.getInputStream());
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public void sendMessage(Message message) throws IOException {
        outputStream.writeMessage(message);
    }

    public void closeConnection(){
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}

