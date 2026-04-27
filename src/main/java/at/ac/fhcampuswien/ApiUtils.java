package at.ac.fhcampuswien;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiUtils {
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static Map<String, String> parseQueryParams(String query){
        Map<String, String> map = new HashMap<>();
        if(query == null || query.isEmpty()) return map;              //Handling error case if no input
        String[] pairs = query.split("&");                      //query pairs get split up
        for(String pair : pairs){
            String[] kv = pair.split("=", 2);
            if(kv.length == 2) map.put(kv[0], kv[1]);                 //Only include key:value pairs
        }
        return map;
    }
}
