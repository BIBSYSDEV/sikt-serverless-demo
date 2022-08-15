package no.unit.nva.bergen;

import nva.commons.core.Environment;

public final class EventsConfig {
    
    public static final Environment ENVIRONMENT = new Environment();
    
    public static final String EVENTS_BUCKET = ENVIRONMENT.readEnv("EVENTS_BUCKET");
    public static final String EVENT_BUS_NAME = ENVIRONMENT.readEnv("EVENT_BUS_NAME");
    
    private EventsConfig() {
    
    }
}
