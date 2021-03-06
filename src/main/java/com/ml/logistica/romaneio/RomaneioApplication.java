package com.ml.logistica.romaneio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class RomaneioApplication {

    private static final Logger log = LoggerFactory.getLogger(RomaneioApplication.class);
    @Inject
    private Environment env;

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(RomaneioApplication.class);
        Environment env = app.run(args).getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application is running! Access URLs:\n\t" +
                        "Local: \t\thttp://127.0.0.1:{}\n\t" +
                        "External: \thttp://{}:{}\n" +
                        "----------------------------------------------------------",
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }
}
