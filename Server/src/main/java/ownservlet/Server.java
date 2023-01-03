package ownservlet;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

class ServerSomthing extends Thread {

    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток завписи в сокет

    /**
     * для общения с клиентом необходим сокет (адресные данные)
     *
     * @throws IOException
     */

    public void printStory(BufferedWriter writer, int size) {
        try {
            writer.write(size + 1 + "\n");
            writer.flush();
        } catch (IOException ignored) {
        }
    }

    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        printStory(out, Server.serverList.size()); // поток вывода передаётся для передачи истории последних 10
        // сооюбщений новому поключению
        start(); // вызываем run()
    }

    @Override
    public void run() {
        String word;
        try {
            try {
                while (true) {
                    word = in.readLine();
                    if (word.equals("pause")) {
                        for (ServerSomthing vr : Server.serverList) {
                            vr.send(word); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                        }
                    } else {
                        for (ServerSomthing vr : Server.serverList) {
                            if (this == vr) {
                                continue;
                            }
                            vr.send(word);
                        }
                    }
                }
            } catch (NullPointerException ignored) {
            }


        } catch (IOException e) {
            this.downService();
        }
    }

    /**
     * отсылка одного сообщения клиенту по указанному потоку
     *
     * @param msg
     */
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * закрытие сервера
     * прерывание себя как нити и удаление из списка нитей
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerSomthing vr : Server.serverList) {
                    if (vr.equals(this)) vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {
        }
    }
}

public class Server {

    public static final int PORT = 8080;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");
        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomthing(socket)); // добавить новое соединенние в список
                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}
