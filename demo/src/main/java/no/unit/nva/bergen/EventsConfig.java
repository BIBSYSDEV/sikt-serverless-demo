package no.unit.nva.bergen;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public final class EventsConfig {
    
    public static final Environment ENVIRONMENT = new Environment();
    
    public static final String EVENTS_BUCKET = ENVIRONMENT.readEnv("EVENTS_BUCKET");
    public static final String EVENT_BUS_NAME = ENVIRONMENT.readEnv("EVENT_BUS_NAME");
    public static final Region AWS_REGION = extractAwsRegion();
    
    private EventsConfig() {
    
    }
    
    private static Region extractAwsRegion() {
        var envValue = ENVIRONMENT.readEnv("AWS_REGION");
        return Region.of(envValue);
    }
    
    @JacocoGenerated
    public static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder()
            .region(AWS_REGION)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
    }
}
