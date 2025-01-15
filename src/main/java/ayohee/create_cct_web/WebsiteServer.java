package ayohee.create_cct_web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebsiteServer {
    private static Thread threadInst;
    private static HttpServer serverInst;

    public static void start() {
        CreateCCTWebInterface.LOGGER.info("Website server starting...");
        threadInst = new Thread(() -> {
            try {
                serverThread();
            } catch (IOException ioe) {
                StringWriter sw = new StringWriter();
                ioe.printStackTrace(new PrintWriter(sw));
                CreateCCTWebInterface.LOGGER.error("Website server encountered exception!");
                CreateCCTWebInterface.LOGGER.error(sw.toString());
            }

        });
        threadInst.start();
    }
    public static void restart() {
        CreateCCTWebInterface.LOGGER.info("Website server restarting...");
        kill();
        start();
    }
    public static void kill() {
        CreateCCTWebInterface.LOGGER.info("Killing website server...");
        if (serverInst != null && threadInst.isAlive()) {
            serverInst.stop(0);
            threadInst.stop();
        }
    }

    public static void serverThread() throws IOException {
        serverInst = HttpServer.create(new InetSocketAddress(8081), 0);

        Path staticRoot = Path.of("mods/CCCTWI_static/");
        for(Path p : Files.walk(staticRoot).filter(Files::isRegularFile).toList()) {
            String location = "/" + staticRoot.relativize(p);
            if (location.contentEquals("/index.html")) {
                location = "/";
            }

            CreateCCTWebInterface.LOGGER.info("Found static file: " + p + ": routing as " + location);
            serverInst.createContext(location, new StaticFileHandler(p));
        }

        serverInst.setExecutor(null); // creates a default executor
        serverInst.start();
    }

    static class StaticFileHandler implements HttpHandler {
        private Path staticPath;

        public StaticFileHandler(Path filepath) {
            staticPath = filepath;
        }

        public void handle(HttpExchange t) throws IOException {
            String response = Files.readString(staticPath);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
