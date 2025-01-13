package ayohee.create_cct_web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlServer {
    private static Thread inst;

    //TODO replace with separate process and executable.jar so it can be hot-replaced
    public static void start() {
        CreateCCTWebInterface.LOGGER.info("Control server starting...");
        inst = new Thread(() -> {
            try {
                serverThread();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                CreateCCTWebInterface.LOGGER.error("Control server encountered exception! restarting...");
                CreateCCTWebInterface.LOGGER.error(sw.toString());
            }
        });
        inst.start();
    }
    public static void restart() {
        CreateCCTWebInterface.LOGGER.info("Control server restarting...");
        inst.stop();
        start();
    }
    public static void kill() {
        CreateCCTWebInterface.LOGGER.info("Killing control server...");
        inst.stop();
    }


    public static void serverThread() throws IOException, NoSuchAlgorithmException {
        ServerSocket server = new ServerSocket(8082);
        try {
            System.out.println("Server has started on 127.0.0.1:8082.\r\nWaiting for a connection…");
            Socket client = server.accept();
            System.out.println("A client connected.");
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            Scanner s = new Scanner(in, "UTF-8");

            try {
                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                            + "\r\n\r\n").getBytes("UTF-8");
                    out.write(response, 0, response.length);
                    byte[] decoded = new byte[6];
                    byte[] encoded = new byte[] { (byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135 };
                    byte[] key = new byte[] { (byte) 167, (byte) 225, (byte) 225, (byte) 210 };
                    for (int i = 0; i < encoded.length; i++) {
                        decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
                    }
                }
            } finally {
                s.close();
            }
        } finally {
            server.close();
        }
    }
}
