package no.unit.nva.bergen;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public class DemoApiGatewayRestHandler extends ApiGatewayHandler<Void, String> {
    
    public static final String EVENT_TOPIC = "Demo.Message.Http";
    public static final String SECRET_NAME = new Environment().readEnv("APPLICATION_SECRET_NAME");
    public static final String SECRET_KEY = new Environment().readEnv("APPLICATION_SECRET_KEY");
    private final SecretsReader secretsReader;
    
    @JacocoGenerated
    public DemoApiGatewayRestHandler() {
        this(SecretsReader.defaultSecretsManagerClient());
    }
    
    @JacocoGenerated
    public DemoApiGatewayRestHandler(SecretsManagerClient secretsManagerClient) {
        super(Void.class);
        this.secretsReader = new SecretsReader(secretsManagerClient);
    }
    
    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) {
        return secretsReader.fetchSecret(SECRET_NAME, SECRET_KEY);
    }
    
    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return HTTP_ACCEPTED;
    }
}
