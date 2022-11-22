package com.redis.splunk.forwarder.proxy.service;

import com.redis.splunk.forwarder.util.SSLUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Service
public class HECProxyService {
    @Value("${proxy.target.schema}")
    String schema = "https";
    @Value("${proxy.target.host}")
    String host = "localhost";
    @Value("${proxy.target.port}")
    Integer port = 8088;
    final static String HEC = "/services/collector/event";
    private final static Logger logger = LogManager.getLogger(HECProxyService.class);

    public ResponseEntity processProxyRequest(String requestUrl, HttpMethod method, HttpHeaders headers, String body) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        //ThreadContext.put("traceId", traceId);

        //log if required in this line
        URI uri = new URI(schema, null, host, port, null, null, null);

        // replacing context path form urI to match actual gateway URI
        uri = UriComponentsBuilder.fromUri(uri)
                .path(requestUrl)
                .query(null)
                .build(true).toUri();

        //headers.set("TRACE", traceId);
        headers.remove(HttpHeaders.ACCEPT_ENCODING);

        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        SSLUtil.turnOffSslChecking();
        RestTemplate restTemplate = new RestTemplate(factory);

        if(!body.isEmpty() && requestUrl.equals(HEC)){
            HttpEntity<String> httpEntityJson = new HttpEntity<>(body, headers);
            return getResponseEntityString(method, uri, httpEntityJson, restTemplate);
        } else {
            HttpEntity<String> httpEntityRaw = new HttpEntity<>(body, headers);
            return getResponseEntityString(method, uri, httpEntityRaw, restTemplate);
        }
    }

    private ResponseEntity<String> getResponseEntityString(HttpMethod method, URI uri, HttpEntity<String> httpEntity, RestTemplate restTemplate) {
        try {
            ResponseEntity<String> serverResponse = restTemplate.exchange(uri, method, httpEntity, String.class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
            logger.info(serverResponse);
            return serverResponse;
        } catch (HttpStatusCodeException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}