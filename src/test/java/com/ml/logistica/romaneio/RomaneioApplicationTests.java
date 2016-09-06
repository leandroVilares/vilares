package com.ml.logistica.romaneio;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;


@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RomaneioApplication.class)
@ConfigurationProperties(prefix = "ml.logistica.api.romaneio.service")
public class RomaneioApplicationTests {

    private String baseUrl;
    private String authToken;


    private HttpHeaders headers = null;
    private HttpEntity<?> request = null;
    private RestTemplate rest = null;
    private String serviceURL = null;
    private ResponseEntity<Object> response = null;

    @Inject
    private Environment env;

    @Test
    public void findCourrierByDate() {
        response = rest.exchange(serviceURL + "?branch.id=991&createdat_greater=2016-05-19T00:00:00", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void findCourrierById() {

        response = rest.exchange(serviceURL + "?branch.id=991&id=812086", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

        response = rest.exchange(serviceURL + "?type.id=2&branch.id=991&id=812086", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

    }

    @Test
    public void findInvalidParameter() {

        response = rest.exchange(serviceURL + "?branch.id=asdf&id=123", HttpMethod.GET, request, Object.class);
        Assert.assertFalse(response.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void findNotFound() {
        response = rest.exchange(serviceURL + "?branch.id=123&id=123", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND));
    }

//    @Test
//    public void findTraditionalByDate() {
//        response = rest.exchange(serviceURL + "?type.id=1&branch.id=300&createdate_lesser=2016-05-10T00:00:00", HttpMethod.GET, request, Object.class);
//        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
//    }

    @Test
    public void findTraditionalById() {

        response = rest.exchange(serviceURL + "?branch.id=300&id=2792789", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

        response = rest.exchange(serviceURL + "?type.id=1&branch.id=300&id=2792789", HttpMethod.GET, request, Object.class);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    @Before
    public void setup() {
        headers = new HttpHeaders();
        request = new HttpEntity<>(headers);
        response = null;
        serviceURL = "http://127.0.0.1:" + env.getProperty("server.port") + "/" + baseUrl;

        rest = new RestTemplate();
        rest.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // Do Nothing
            }
        });

    }

}
