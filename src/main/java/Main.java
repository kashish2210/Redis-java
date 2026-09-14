import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import Components.Infra.Slave;
import Components.Server.MasterTcpServer;
import Components.Server.RedisConfig;
import Components.Server.SlaveTcpServer;
import Config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    private static final int DEFAULT_PORT = 6379;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        try {
            MasterTcpServer master = context.getBean(MasterTcpServer.class);

            SlaveTcpServer slave = context.getBean(SlaveTcpServer.class);

            RedisConfig redisConfig = context.getBean(RedisConfig.class);

            // Default configuration
            redisConfig.setPort(DEFAULT_PORT);
            redisConfig.setRole("master");

            // Parse command-line arguments
            parseArguments(args, redisConfig);

            // Start appropriate server
            if ("slave".equals(redisConfig.getRole())) {
                System.out.println(
                        "Starting Redis server as SLAVE on port "
                                + redisConfig.getPort());

                System.out.println(
                        "Master: "
                                + redisConfig.getMasterHost()
                                + ":"
                                + redisConfig.getMasterPort());

                slave.startServer();

            } else {

                System.out.println(
                        "Starting Redis server as MASTER on port "
                                + redisConfig.getPort());

                master.startServer();
            }

        } catch (Exception e) {

            System.err.println("Failed to start server.");
            e.printStackTrace();

        } finally {

            context.close();
        }
    }

    private static void parseArguments(
            String[] args,
            RedisConfig redisConfig) {

        for (int i = 0; i < args.length; i++) {

            String argument = args[i];

            switch (argument) {

                case "--port":

                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException(
                                "Missing value for --port");
                    }

                    int port = parsePort(args[++i]);

                    redisConfig.setPort(port);
                    break;

                case "--replicaof":

                    if (i + 2 >= args.length) {
                        throw new IllegalArgumentException(
                                "Usage: --replicaof <master-host> <master-port>");
                    }

                    String masterHost = args[++i];
                    int masterPort = parsePort(args[++i]);

                    redisConfig.setRole("slave");
                    redisConfig.setMasterHost(masterHost);
                    redisConfig.setMasterPort(masterPort);

                    break;

                default:

                    throw new IllegalArgumentException(
                            "Unknown argument: " + argument);
            }
        }
    }

    private static int parsePort(String value) {

        try {

            int port = Integer.parseInt(value);

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(
                        "Port must be between 1 and 65535: " + port);
            }

            return port;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid port number: " + value);
        }
    }
}
}
