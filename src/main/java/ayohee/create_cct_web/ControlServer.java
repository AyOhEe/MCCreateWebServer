package ayohee.create_cct_web;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ControlServer {
    private static Process apiServerInstance;

    public static void start() {
        CreateCCTWebInterface.LOGGER.info("Control server starting...");
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", getServerJarName());
            pb.directory(null);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            apiServerInstance = pb.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            CreateCCTWebInterface.LOGGER.error("Control server encountered exception!");
            CreateCCTWebInterface.LOGGER.error(sw.toString());
        }
    }

    public static void restart() {
        CreateCCTWebInterface.LOGGER.info("Control server restarting...");
        kill();
        start();
    }
    public static void kill() {
        CreateCCTWebInterface.LOGGER.info("Killing control server...");
        if (apiServerInstance.isAlive()) {
            apiServerInstance.destroy();
        }
    }

    private static String getServerJarName() throws IOException {
        Path lookIn = Path.of("mods/CCCTWI_api/");
        List<Path> matches = Files.find(lookIn, 1, (p, f) -> f.isRegularFile(), new FileVisitOption[0]).toList();
        if (matches.isEmpty()) {
            throw new FileNotFoundException("Could not locate API .jar file!");
        }
        return matches.get(0).toString();
    }
}
