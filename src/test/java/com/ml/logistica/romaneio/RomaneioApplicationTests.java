package com.ml.logistica.romaneio;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Profile({"test" })
@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest
@WebAppConfiguration
@SpringApplicationConfiguration(classes = RomaneioApplication.class)
@ConfigurationProperties(prefix = "ml.logistica.api.romaneio.service")
public class RomaneioApplicationTests {

	private String baseUrl;
	private String authToken;

	@Test
	public void queryRomaneioById() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Auth", authToken);

		HttpEntity<?> request = new HttpEntity<Object>(headers);

		RestTemplate rest = new RestTemplate();
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
		ResponseEntity<Object> response = null;

		String serviceURL = "http://127.0.0.1:8080/"+baseUrl;
		
		// get packing list
		response = rest.exchange(serviceURL + "?branch.id=300&id=2792789", HttpMethod.GET, request, Object.class);
		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		// Tradicional
		response = rest.exchange(serviceURL + "?type.id=1&branch.id=300&id=2792789", HttpMethod.GET, request,
				Object.class);
		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		response = rest.exchange(serviceURL + "?branch.id=991&id=812086", HttpMethod.GET, request, Object.class);
		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		// Courrier
		response = rest.exchange(serviceURL + "?type.id=2&branch.id=991&id=812086", HttpMethod.GET, request, Object.class);
		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

//		response = rest.exchange(serviceURL + "?branch.id=991&createdat_greater=2016-05-19 00:00:00", HttpMethod.GET,
//				request, Object.class);
//		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
//		// Tradicional
//		response = rest.exchange(serviceURL + "?type.id=1&branch.id=300&createdat_lesser=2016-05-10 00:00:00",
//				HttpMethod.GET, request, Object.class);
//		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		// Errors
		// Not found
		response = rest.exchange(serviceURL + "?branch.id=123&id=123", HttpMethod.GET, request, Object.class);
		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND));
		// ERROR
		response = rest.exchange(serviceURL + "?branch.id=asdf&id=123", HttpMethod.GET, request, Object.class);
		Assert.assertFalse(response.getStatusCode().equals(HttpStatus.OK));

//		response = rest.exchange(
//				serviceURL + "?type.id=1&branch.id=300&createdat_greater=2016-05-01 00:00:00&createdat_lesser=2016-05-10 00:00:00",
//				HttpMethod.GET, request, Object.class);
//		Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));
//
//		response = rest.exchange(
//				serviceURL + "?type.id=1&branch.id=300&createdat_greater=2016-05-02 00:00:00&createdat_lesser=2016-05-01 00:00:00",
//				HttpMethod.GET, request, Object.class);
//		Assert.assertFalse(response.getStatusCode().equals(HttpStatus.OK));

	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

}
