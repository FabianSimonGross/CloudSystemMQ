package de.nimzan;

import de.nimzan.master.MasterApp;
import de.nimzan.node.app.NodeApp;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws UnknownHostException {

        boolean runNode = false;
        boolean runMaster = false;

        if (args.length == 0) {
            log.info("No args given, starting both");
            runNode = true;
            runMaster = true;
        } else {
            for (String arg : args) {
                if ("--node".equalsIgnoreCase(arg))  {
                    log.info("Starting node");
                    runNode = true;
                }
                if ("--master".equalsIgnoreCase(arg)) {
                    log.info("Starting master");
                    runMaster = true;
                }
            }
        }

        // Optional but recommended: remove our custom flags before giving args to Spring
        String[] springArgs = Arrays.stream(args)
                .filter(a -> !a.equalsIgnoreCase("--node") && !a.equalsIgnoreCase("--master"))
                .toArray(String[]::new);

        if (runNode) {
            Thread nodeThread = new Thread(new NodeApp(), "Node-Thread");
            nodeThread.setDaemon(false);
            nodeThread.start();
        }

        if (runMaster) {
            SpringApplication.run(MasterApp.class, springArgs);
        }

        if (!runNode && !runMaster) {
            log.warn("Usage: java -jar app.jar [--node] [--master]");
            log.warn("No args = start both");
        }
    }
}
