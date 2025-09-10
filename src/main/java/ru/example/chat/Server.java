package ru.example.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Server {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String LOG_FILE = "file.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final int port;
    private final List<ClientHandler> clients = new ArrayList<>();

    public Server() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(SETTINGS_FILE));
        this.port = Integer.parseInt(props.getProperty("port"));
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
        log(message);
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private void log(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(DATE_FORMAT.format(new Date()) + " " + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    static class ClientHandler extends Thread {
        private final Socket socket;
        private final Server server;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Запрос имени
                out.println("Enter your name:");
                username = in.readLine();

                // Явная проверка на null и пустоту, чтобы избежать warnings в IDE
                if (username == null) {
                    username = "Anonymous";
                } else if (username.trim().isEmpty()) {
                    username = "Anonymous";
                }

                // Добавляем клиента в список перед broadcast
                server.addClient(this);
                server.broadcast(username + " has joined the chat");

                // Чтение сообщений
                String message;
                while ((message = in.readLine()) != null) {
                    if ("/exit".equalsIgnoreCase(message)) {
                        break;
                    }
                    server.broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.removeClient(this);
                if (username != null) {
                    server.broadcast(username + " has left the chat");
                }
                // Явное закрытие потоков (для лучшей практики)
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String message) {
            out.println(message);  // Без добавления даты, как в предыдущей версии
        }
    }
}