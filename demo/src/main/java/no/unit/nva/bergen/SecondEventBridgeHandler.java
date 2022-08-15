package no.unit.nva.bergen;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class SecondEventBridgeHandler extends DestinationsEventBridgeEventHandler<EventReference, Void> {
    
    private static final Logger logger = LoggerFactory.getLogger(SecondEventBridgeHandler.class);
    private final S3Driver s3Driver;
    
    @JacocoGenerated
    public SecondEventBridgeHandler() {
        this(S3Driver.defaultS3Client().build());
    }
    
    public SecondEventBridgeHandler(S3Client s3Client) {
        super(EventReference.class);
        this.s3Driver = new S3Driver(s3Client, EventsConfig.EVENTS_BUCKET);
    }
    
    @Override
    protected Void processInputPayload(EventReference input,
                                       AwsEventBridgeEvent<AwsEventBridgeDetail<EventReference>> event,
                                       Context context) {
        var message = s3Driver.readEvent(input.getUri());
        logger.info(formatLogMessage(message));
        return null;
    }
    
    private String formatLogMessage(String message) {
        return String.format("The message was:%s", message);
    }
}
