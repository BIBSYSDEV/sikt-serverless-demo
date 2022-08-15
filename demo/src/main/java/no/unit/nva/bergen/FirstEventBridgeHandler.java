package no.unit.nva.bergen;

import static no.unit.nva.bergen.EventsConfig.EVENTS_BUCKET;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.util.Locale;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public class FirstEventBridgeHandler extends EventHandler<EventReference, EventReference> {
    
    public static final String EVENT_TOPIC = "Demo.Message.Capitalize";
    private final S3Driver s3Driver;
    
    @JacocoGenerated
    public FirstEventBridgeHandler() {
        this(S3Driver.defaultS3Client().build());
    }
    
    protected FirstEventBridgeHandler(S3Client s3Client) {
        super(EventReference.class);
        this.s3Driver = new S3Driver(s3Client, EVENTS_BUCKET);
    }
    
    @Override
    protected EventReference processInput(EventReference input,
                                          AwsEventBridgeEvent<EventReference> event,
                                          Context context) {
        var inputEvent = extractEventBodyFromEvent(input);
        var outputEvent = businessLogic(inputEvent);
        return emitNewEvent(outputEvent);
    }
    
    private Message extractEventBodyFromEvent(EventReference input) {
        var eventBodyString = s3Driver.readEvent(input.getUri());
        return Message.fromJson(eventBodyString);
    }
    
    private Message businessLogic(Message message) {
        var newText = message.getText().toUpperCase(Locale.getDefault());
        return new Message(newText);
    }
    
    private EventReference emitNewEvent(Message outputEvent) {
        var newEventBodyUri = sendEvent(outputEvent);
        return new EventReference(EVENT_TOPIC, newEventBodyUri);
    }
    
    private URI sendEvent(Message newMessage) {
        return attempt(() -> s3Driver.insertEvent(UnixPath.EMPTY_PATH, newMessage.toString()))
            .orElseThrow();
    }
}
