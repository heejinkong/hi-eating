package kr.or.hieating.email.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "greenfood.email-event")
public record EmailEventProperties(
    boolean enabled,
    String exchange,
    String routingKey,
    String queue,
    Duration publishConfirmTimeout) {}
