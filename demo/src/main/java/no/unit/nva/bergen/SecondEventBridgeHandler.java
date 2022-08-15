package no.unit.nva.bergen;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class SecondEventBridgeHandler extends DestinationsEventBridgeEventHandler<EventReference, Void> {
    
    private final S3Driver s3Driver;
    
    @JacocoGenerated
    public SecondEventBridgeHandler() {
        super(EventReference.class);
        var s3Client = S3Driver.defaultS3Client().build();
        this.s3Driver = new S3Driver(s3Client,EventsConfig.EVENTS_BUCKET);
    }
    
    @JacocoGenerated
    @Override
    protected Void processInputPayload(EventReference input,
                                       AwsEventBridgeEvent<AwsEventBridgeDetail<EventReference>> event,
                                       Context context) {
        var message = s3Driver.readEvent(input.getUri());
        System.out.println(message);
        return null;
    }
}
