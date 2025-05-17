package tech.kitucode.banking;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private String ibanPrefix;
    private Integer maxCardsPerAccount;
}
