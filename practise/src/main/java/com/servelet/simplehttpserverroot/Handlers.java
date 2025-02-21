package com.servelet.simplehttpserverroot;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Handlers {
    private static final Map<String, String> userDatabase = new HashMap<>(); // In-memory user storage

    public static class RootHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response="<h1> Server start success if you see this message</h1>"+"<h1>Port: "+SimpleHttpServer.Default_Port+"</h1>";
            exchange.sendResponseHeaders(200,response.length());
            OutputStream os=exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    public static class EchoHeaderHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers=exchange.getRequestHeaders();
            Set<Map.Entry<String, List<String>>> entries=headers.entrySet();
            String response="";
            for(Map.Entry<String,List<String>> entry:entries){
                response+=entry.toString()+"\n";
            }
            exchange.sendResponseHeaders(200,response.length());
            OutputStream os=exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }}
    private static void parseQuery(String query,Map<String,Object> parameters)throws UnsupportedEncodingException{
        if(query!=null){
            String pairs[]=query.split("[&]");
            for(String pair:pairs){
                String param[]=pair.split("[=]");
                String key=null;
                String value=null;
                if(param.length>0){
                    key= URLDecoder.decode(param[0],System.getProperty("file.encoding"));

                }
                if(param.length>1){
                    value= URLDecoder.decode(param[1],System.getProperty("file.encoding"));

                }
                parameters.put(key,value);
            }
        }
    }
    private static Map<String, String> parseQuery1(String query) throws UnsupportedEncodingException {
        Map<String, String> parameters = new HashMap<>();

        if (query != null) {
            String[] pairs = query.split("&");

            for (String pair : pairs) {
                String[] param = pair.split("=");
                if (param.length > 1) {
                    // ✅ Fix applied: "UTF-8" String instead of StandardCharsets.UTF_8
                    String key = URLDecoder.decode(param[0], "UTF-8");
                    String value = URLDecoder.decode(param[1], "UTF-8");
                    parameters.put(key, value);
                } else if (param.length == 1) {
                    // Handle case where there's a key with no value
                    String key = URLDecoder.decode(param[0], "UTF-8");
                    parameters.put(key, "");
                }
            }
        }

        return parameters;
    }


    public static class EchoGetHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> parameters=new HashMap<>();
            URI requestedUri=exchange.getRequestURI();
            String query=requestedUri.getRawQuery();
            parseQuery(query,parameters);
            String response="";
            for (String key: parameters.keySet())
                response+=key+" = "+parameters.get(key)+"\n";
            exchange.sendResponseHeaders(200,response.length());
            OutputStream os=exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }}
    public static class EchoPostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String,Object> parameters = new HashMap<>();

            // ✅ Corrected InputStreamReader
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);

            String query = br.readLine();
            parseQuery(query, parameters);

            String response = "";
            for(String key : parameters.keySet()){
                response += key + " = " + parameters.get(key) + "\n";
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static class UserRegistrationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Read request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();

            Map<String, String> parameters = parseQuery1(query);
            String username = parameters.get("username");
            String password = parameters.get("password");

            String response;
            if (username == null || password == null) {
                response = "Error: Missing username or password!";
                exchange.sendResponseHeaders(400, response.length()); // Bad Request
            } else {
                if (userDatabase.containsKey(username)) {
                    response = "Error: User already exists!";
                    exchange.sendResponseHeaders(409, response.length()); // Conflict
                } else {
                    userDatabase.put(username, password); // Store user in memory
                    response = "User registered successfully!";
                    exchange.sendResponseHeaders(200, response.length());
                }
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static class UserLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();

            Map<String, String> parameters = parseQuery1(query);
            String username = parameters.get("username");
            String password = parameters.get("password");

            String response;
            if (username == null || password == null) {
                response = "Error: Missing username or password!";
                exchange.sendResponseHeaders(400, response.length()); // Bad Request
            } else {
                if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
                    response = "Login successful!";
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "Invalid username or password!";
                    exchange.sendResponseHeaders(401, response.length()); // Unauthorized
                }
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

