package ru.example.chat;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ServerTest {
    @Test
    public void testServerPortLoading() throws Exception {
        // Тест загрузки порта (предполагаем settings.txt существует)
        Server server = new Server();
        // Проверяем, что порт загрузился правильно (из settings.txt)
        // assertEquals(8080, server.port); // Раскомментировать и адаптировать
    }
}