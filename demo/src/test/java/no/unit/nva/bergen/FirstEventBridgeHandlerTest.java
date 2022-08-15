package no.unit.nva.bergen;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import no.unit.nva.testutils.EventBridgeEventBuilder;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FirstEventBridgeHandlerTest {
    
    private ByteArrayOutputStream outputStream;
    private Context context;
    private FakeS3Client s3Client;
    private S3Driver s3Driver;
    private FirstEventBridgeHandler handler;
    
    @BeforeEach
    public void setup() {
        this.outputStream = new ByteArrayOutputStream();
        this.context = createFakeContextWithMethodNameAndFunctionArn();
        this.s3Client = new FakeS3Client();
        this.s3Driver = new S3Driver(s3Client, "notmportant");
        this.handler = new FirstEventBridgeHandler(s3Client);
    }
    
    @Test
    void shouldConsumeEventsWithMessages() throws IOException {
        var firstEventBodyUri = createAlreadyEmittedEventBody();
        var event = new EventReference(DemoHttpHandler.EVENT_TOPIC, firstEventBodyUri);
        var request = EventBridgeEventBuilder.sampleEvent(event);
        handler.handleRequest(request, outputStream, context);
        var response = EventReference.fromJson(outputStream.toString());
        var nextEventBodyUri = response.getUri();
        var oldMessage = readFile(firstEventBodyUri);
        var newMessage = readFile(nextEventBodyUri);
        assertThat(newMessage.getText(), is(equalTo(oldMessage.getText().toUpperCase(Locale.getDefault()))));
    }
    
    private Message readFile(URI fileUri) {
        var messageString = s3Driver.readEvent(fileUri);
        return Message.fromJson(messageString);
    }
    
    private URI createAlreadyEmittedEventBody() throws IOException {
        Message message = new Message(randomString());
        return s3Driver.insertEvent(UnixPath.of(randomString()), message.toString());
    }
    
    private FakeContext createFakeContextWithMethodNameAndFunctionArn() {
        return new FakeContext() {
            @Override
            public String getFunctionName() {
                return randomString();
            }
            
            @Override
            public String getInvokedFunctionArn() {
                return randomString();
            }
        };
    }
}