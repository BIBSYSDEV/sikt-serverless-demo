package no.unit.nva.bergen;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeEventBridgeClient;
import no.unit.nva.stubs.FakeS3Client;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.SingletonCollector;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

class DemoHttpHandlerTest {
    
    private ByteArrayOutputStream output;
    private Context context;
    private DemoHttpHandler handler;
    private FakeEventBridgeClient eventBridgeClient;
    private FakeS3Client s3Client;
    
    @BeforeEach
    public void setup() {
        this.output = new ByteArrayOutputStream();
        this.context = createFakeContextWithMethodNameAndFunctionArn();
        this.eventBridgeClient = new FakeEventBridgeClient();
        this.s3Client = new FakeS3Client();
        this.handler = new DemoHttpHandler(eventBridgeClient, s3Client);
    }
    
    @Test
    void shouldAcceptMessageFromClient() throws IOException {
        var message = randomString();
        var request = new HandlerRequestBuilder<Message>(JsonUtils.dtoObjectMapper)
            .withBody(new Message(message))
            .build();
        handler.handleRequest(request, output, context);
        var response = GatewayResponse.fromOutputStream(output, Void.class);
        assertThat(response.getStatusCode(), is(equalTo(HTTP_ACCEPTED)));
    }
    
    @Test
    void shouldEmitMessageTextInEventBridge() throws IOException {
        var expectedMessageText = randomString();
        var request = new HandlerRequestBuilder<Message>(JsonUtils.dtoObjectMapper)
            .withBody(new Message(expectedMessageText))
            .build();
        handler.handleRequest(request, output, context);
        
        var eventReference = eventBridgeClient.getRequestEntries().stream()
            .map(PutEventsRequestEntry::detail)
            .map(EventReference::fromJson)
            .collect(SingletonCollector.collect());
        var messageString = readEventBody(eventReference);
        var actualMessage = Message.fromJson(messageString);
        
        assertThat(actualMessage.getText(), is(equalTo(expectedMessageText)));
    }
    
    private String readEventBody(EventReference eventReference) {
        var bodyUri = eventReference.getUri();
        var s3Driver = new S3Driver(s3Client, "not_important");
        return s3Driver.readEvent(bodyUri);
    }
    
    private FakeContext createFakeContextWithMethodNameAndFunctionArn() {
        return new FakeContext() {
            @Override
            public String getFunctionName() {
                return "DemoHttpHandler";
            }
            
            @Override
            public String getInvokedFunctionArn() {
                return randomString();
            }
        };
    }
}