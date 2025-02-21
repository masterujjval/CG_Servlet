package com.servelet.simplehttpserverroot;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Handler;

public class SimpleHttpServer {
    public static int Default_Port=9000;
    public static int port;
    private HttpServer httpServer;
    private void start(int port){
        this.port=port;
        try{
            httpServer =HttpServer.create(new InetSocketAddress(port),0);
            System.out.println("server started at "+ port);
            httpServer.createContext("/",new Handlers.RootHandler());
            httpServer.createContext("/echoHeader",new Handlers.EchoHeaderHandler() );
            httpServer.createContext("/echoGet",new Handlers.EchoGetHandler());
            httpServer.createContext("/echoPost",new Handlers.EchoPostHandler());
            httpServer.createContext("/register", new Handlers.UserRegistrationHandler()); // New user registration route
            httpServer.createContext("/login", new Handlers.UserLoginHandler()); // New user registration route

            httpServer.setExecutor(null);

            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SimpleHttpServer httpServer1=new SimpleHttpServer();
        httpServer1.start(Default_Port);
    }
}