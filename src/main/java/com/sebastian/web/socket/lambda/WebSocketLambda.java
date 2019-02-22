package com.sebastian.web.socket.lambda;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * web sockets con aws lambdas.
 *
 * @author Sebastian Avila A.
 */
public class WebSocketLambda implements RequestStreamHandler {

    private static final AWSCredentials CREDENCIALES
            = new EnvironmentVariableCredentialsProvider().getCredentials();

    private static String leerError(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    @Override
    public void handleRequest(InputStream in, OutputStream out, Context cntxt) throws IOException {
        JsonObject json = Json.createReader(in).readObject();
        System.out.println(json);
        if (json.getJsonObject("requestContext").getString("routeKey").equals("$default")) {
            final String payload = json.getString("body");
            Request<Void> request = new DefaultRequest<>("execute-api");
            request.setHttpMethod(HttpMethodName.POST);
            request.setEndpoint(URI.create(new StringBuilder()
                    .append(System.getenv("url_api"))
                    .append("/")
                    .append(json.getJsonObject("requestContext").getString("connectionId"))
                    .toString()));
            request.setContent(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName(request.getServiceName());
            signer.sign(request, CREDENCIALES);
            new AmazonHttpClient(new ClientConfiguration())
                    .requestExecutionBuilder()
                    .executionContext(new ExecutionContext(true))
                    .request(request)
                    .errorResponseHandler(new HttpResponseHandler<AmazonClientException>() {
                        @Override
                        public AmazonClientException handle(HttpResponse rsp) throws Exception {
                            return new AmazonClientException(
                                    new StringBuilder()
                                            .append("error al enviar mensaje")
                                            .append(System.getenv("url_api"))
                                            .append(rsp.getStatusCode())
                                            .append(rsp.getStatusText())
                                            .append(leerError(rsp.getContent())).toString());
                        }

                        @Override
                        public boolean needsConnectionLeftOpen() {
                            return false;
                        }
                    }).execute(new HttpResponseHandler<Void>() {
                @Override
                public Void handle(HttpResponse response) {
                    return null;
                }

                @Override
                public boolean needsConnectionLeftOpen() {
                    return false;
                }
            });
        }
        try (PrintWriter pw = new PrintWriter(out)) {
            JsonObject response = Json.createObjectBuilder()
                    .add("statusCode", 200)
                    .build();
            pw.write(response.toString());
        }
    }
}
