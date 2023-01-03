package ownservlet;

import java.net.*;
import java.io.*;

class ClientSomething {

    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String addr; // ip адрес клиента
    private int port; // порт соединения
    private String nickname; // имя клиента
    private String str;
    private String sendMessage = null;
    private String sendMessage2 = null;
    private String sendMessage3 = null;
    private int oldSendMessage2 = 0;
    private int oldSendMessage = 0;
    private boolean isPaused = false;

    public ClientSomething(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //this.pressNickname(); // перед началом необходимо спросит имя
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientSomething.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }

    /**
     * просьба ввести имя,
     * и отсылка эхо с приветсвием на сервер
     */

    private void pressNickname() {
        System.out.print("Press your nick: ");
        try {
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    private boolean needToPause = true;
    private boolean needToSend = true;
    private double oldX = -1.0;
    private double oldY = -1.0;
    private boolean oldSend = false;
    private boolean oldSend2 = false;

    public synchronized void pausePoint() throws InterruptedException {
        while (needToPause) {
            wait();
        }
    }

    public synchronized void pause() {
        needToPause = true;
    }

    public synchronized void unpause() {
        needToPause = false;
        this.notifyAll();
    }

    public synchronized void send(double x, double y) {
        sendMessage = x + "," + y + "";
        if (x == oldX && y == oldY) {
            needToSend = false;
        } else {
            oldX = x;
            oldY = y;
            needToSend = true;
        }
    }

    public synchronized void sendPlay(int i) {
        sendMessage = "play";
        oldSend2 = true;
        needToSend = true;
        if (i != oldSendMessage2) {
            oldSendMessage2 = i;
            oldSend2 = true;
        } else {
            oldSend2 = false;
        }
    }

    public synchronized void sendPause(int i) {
        sendMessage = "pause";
        oldSend = true;
        needToSend = true;
        if (i != oldSendMessage) {
            oldSendMessage = i;
            oldSend = true;
        } else {
            oldSend = false;
        }
    }

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    str = in.readLine(); // ждем сообщения с сервера
                    write(str);
                }
            } catch (IOException e) {
                ClientSomething.this.downService();
            }
        }
    }

    public synchronized String read() {
        return str;
    }

    public synchronized void write(String str) {
        this.str = str;
        read();
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    pausePoint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {

                    if (oldSend2) {
                        out.write(sendMessage3 + "\n"); // отправляем на сервер
                        out.flush(); // чистим
                        oldSend2 = false;
                        continue;
                    }
                    if (oldSend) {
                        out.write(sendMessage2 + "\n"); // отправляем на сервер
                        out.flush(); // чистим
                        oldSend = false;
                        continue;
                    }
                    if (needToSend) {
                        out.write(sendMessage + "\n"); // отправляем на сервер
                        out.flush(); // чистим
                        needToSend = false;
                    }
                } catch (IOException e) {
                    ClientSomething.this.downService(); // в случае исключения тоже харакири
                }
            }
        }
    }
}

public class Client {

    public static String ipAddr = "localhost";
    public static int port = 8080;

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     *
     * @param args
     */

    public static void main(String[] args) {
        new ClientSomething(ipAddr, port);
    }
}
