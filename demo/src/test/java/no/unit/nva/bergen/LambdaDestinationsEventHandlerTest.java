package no.unit.nva.bergen;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import no.unit.nva.testutils.EventBridgeEventBuilder;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LambdaDestinationsEventHandlerTest {
    
    private static final Context CONTEXT = new FakeContext();
    private S3Driver s3Driver;
    private LambdaDestinationsEventHandler handler;
    private ByteArrayOutputStream outputStream;
    
    @BeforeEach
    public void setup() {
        var s3Client = new FakeS3Client();
        this.s3Driver = new S3Driver(s3Client, "notImportant");
        handler = new LambdaDestinationsEventHandler(s3Client);
        this.outputStream = new ByteArrayOutputStream();
    }
    
    @Test
    void shouldLogMessageWhenReceivingANonEmptyMessage() throws IOException {
        var message = new Message(randomString());
        var fileUri = s3Driver.insertEvent(UnixPath.EMPTY_PATH, message.toString());
        var eventReference = new EventReference(GenericEventBridgeHandler.EVENT_TOPIC, fileUri);
        var input =
            EventBridgeEventBuilder.sampleLambdaDestinationsEvent(eventReference);
        var logger = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(input, outputStream, CONTEXT);
        assertThat(logger.getMessages(), containsString(message.getText()));
    }
}