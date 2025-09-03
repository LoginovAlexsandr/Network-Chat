package ru.example.chat;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String LOG_FILE = "file.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String host = "localhost"; // Или другой хост
    private final int port;
    private String username;

    public Client() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(SETTINGS_FILE));
        this.port = Integer.parseInt(props.getProperty("port"));
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // Выбор имени
            System.out.println("Enter your name:");
            username = scanner.nextLine();
            out.println(username);

            // Поток для чтения сообщений с сервера
            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                        log(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Отправка сообщений с консоли
            String message;
            while (scanner.hasNextLine()) {
                message = scanner.nextLine();
                if ("/exit".equalsIgnoreCase(message)) {
                    break;
                }
                out.println(message);
                log(username + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(DATE_FORMAT.format(new Date()) + " " + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Client().start();
    }
}