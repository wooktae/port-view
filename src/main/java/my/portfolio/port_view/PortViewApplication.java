package my.portfolio.port_view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PortViewApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortViewApplication.class, args);
    }
}
