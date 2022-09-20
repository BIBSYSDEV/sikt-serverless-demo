package no.unit.nva.bergen;

import static no.unit.nva.bergen.DemoApiGatewayRestHandler.SECRET_KEY;
import static no.unit.nva.bergen.DemoApiGatewayRestHandler.SECRET_NAME;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeSecretsManagerClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DemoApiGatewayRestHandlerTest {
    
    private ByteArrayOutputStream output;
    private Context context;
    private FakeSecretsManagerClient fakeSecretsClient;
    
    @BeforeEach
    public void setup() {
       this.output =new ByteArrayOutputStream();
       this.context = new FakeContext();
       this.fakeSecretsClient = new FakeSecretsManagerClient();
       
    }
    
    
    @Test
    void shouldReadSecretFromAws() throws IOException {
        var handler = new DemoApiGatewayRestHandler(fakeSecretsClient);
        var expectedSecretValue = randomString();
        fakeSecretsClient.putSecret(SECRET_NAME,SECRET_KEY,expectedSecretValue);
        var request = new HandlerRequestBuilder<Void>(JsonUtils.dtoObjectMapper)
                                  .build();
        handler.handleRequest(request,output,context);
        var response = GatewayResponse.fromOutputStream(output,String.class);
        var secretValue = response.getBody();
        
        assertThat(secretValue,is(equalTo(expectedSecretValue)));
        
    }
    
  
}