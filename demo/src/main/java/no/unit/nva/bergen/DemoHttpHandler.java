package no.unit.nva.bergen;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static no.unit.nva.bergen.EventsConfig.ENVIRONMENT;
import static no.unit.nva.bergen.EventsConfig.EVENTS_BUCKET;
import static no.unit.nva.bergen.EventsConfig.EVENT_BUS_NAME;
import static no.unit.nva.s3.S3Driver.defaultS3Client;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.s3.S3Client;

public class DemoHttpHandler extends ApiGatewayHandler<Message, Void> {
    
    public static final String EVENT_TOPIC = "Demo.Message.Http";
    
    private static final Region AWS_REGION = extractAwsRegion();
    private final EventBridgeClient eventBridgeClient;
    private final S3Client s3Client;
    
    @JacocoGenerated
    public DemoHttpHandler() {
        this(defaultEventBridgeClient(), defaultS3Client().build());
    }
    
    public DemoHttpHandler(EventBridgeClient eventBridgeClient, S3Client s3Client) {
        super(Message.class);
        this.eventBridgeClient = eventBridgeClient;
        this.s3Client = s3Client;
    }
    
    @JacocoGenerated
    public static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder()
            .region(AWS_REGION)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
    }
    
    @Override
    protected Void processInput(Message input, RequestInfo requestInfo, Context context) {
        var event = createEventReferenceForMessage(input);
        insertEventInEventBridge(context, event);
        return null;
    }
    
    @Override
    protected Integer getSuccessStatusCode(Message input, Void output) {
        return HTTP_ACCEPTED;
    }
    
    private static Region extractAwsRegion() {
        var envValue = ENVIRONMENT.readEnv("AWS_REGION");
        return Region.of(envValue);
    }
    
    private void insertEventInEventBridge(Context context, EventReference event) {
        var putEventRequestEntry = PutEventsRequestEntry.builder()
            .detail(event.toJsonString())
            .source(context.getFunctionName())
            .eventBusName(EVENT_BUS_NAME)
            .detailType("not in use")
            .resources(context.getInvokedFunctionArn())
            .build();
        var putEventRequest = PutEventsRequest.builder()
            .entries(putEventRequestEntry)
            .build();
        eventBridgeClient.putEvents(putEventRequest);
    }
    
    private EventReference createEventReferenceForMessage(Message inputMessage) {
        var fileUri = persistMessageToEventsS3Bucket(inputMessage);
        return new EventReference(EVENT_TOPIC, fileUri);
    }
    
    private URI persistMessageToEventsS3Bucket(Message inputMessage) {
        var s3Driver = new S3Driver(s3Client, EVENTS_BUCKET);
        return attempt(() -> s3Driver.insertEvent(UnixPath.EMPTY_PATH,inputMessage.toString())).orElseThrow();
    }
}
