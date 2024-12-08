package com.examples;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@com.github.tomakehurst.wiremock.junit5.WireMockTest
public class WireMockTest {


    @Test
    void testWireMock(WireMockRuntimeInfo wmRuntimeInfo) throws URISyntaxException, IOException, InterruptedException {
        Gson gson = new Gson();
        Car car = new Car("Fiat", "Punto");
        Person person = new Person(null, "John", "Doe");

        stubFor(
                get("/car")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(gson.toJson(car))
                                .withStatus(200)
                        )
        );

        stubFor(
                post("/persons")
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(equalToJson(gson.toJson(person)))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(gson.toJson( new Person("John", "Doe")))
                                .withStatus(201)
                        )
        );

        try (HttpClient client = HttpClient.newBuilder().build()) {
            final HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:" + wmRuntimeInfo.getHttpPort() + "/car"))
                    .header("Content-Type", "text/xml")
                    .GET()
                    .build();

            final HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            String expectedFiat = """
                    {"brand":"Fiat","model":"Punto"}""";
            Assertions.assertEquals(expectedFiat, getResponse.body());


            final HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:" + wmRuntimeInfo.getHttpPort() + "/persons"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(person)))
                    .build();
            final HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            Person createdPerson = gson.fromJson(postResponse.body(), Person.class);
            Assertions.assertNotNull(createdPerson.id());
        }
    }
}
