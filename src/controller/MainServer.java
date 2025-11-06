package controller;

import com.sun.net.httpserver.HttpServer;
import util.LoggingConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer {
    public static void main(String[] args) throws IOException {
        LoggingConfig.setup();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/api/bookings", new BookingController());
        server.setExecutor(null);
        server.start();

        System.out.println("Server started at " + new InetSocketAddress(8000));
    }
}
