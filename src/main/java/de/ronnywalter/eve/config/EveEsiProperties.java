package de.ronnywalter.eve.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class EveEsiProperties {
    @Value( "${oauth2.authorization-uri}" )
    private String authorizationUrl;

    @Value( "${oauth2.token-uri}" )
    private String tokenUrl;


    @Value( "${oauth2.clientId}" )
    private String clientId;

    @Value( "${oauth2.clientSecret}" )
    private String clientSecret;
}
