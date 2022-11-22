package com.redis.splunk.forwarder.proxy.service;

import com.redis.splunk.forwarder.util.SSLUtil;
import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Service
public class S2SProxyService {

    @Value("${proxy.target.schema}")
    String schema = "https";
    @Value("${proxy.target.host}")
    String host = "localhost";
    @Value("${proxy.target.port}")
    Integer port = 8088;

    private final static Logger logger = LogManager.getLogger(S2SProxyService.class);

    public ResponseEntity processProxyRequest(String requestUrl, HttpMethod method, HttpHeaders headers, InputStream body) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
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

        HttpEntity<byte[]> httpEntityByte = new HttpEntity<>(IOUtils.toByteArray(body), headers);
        return getResponseEntityByteArray(method, uri, httpEntityByte, restTemplate);
    }

    private ResponseEntity<byte[]> getResponseEntityByteArray(HttpMethod method, URI uri, HttpEntity<byte[]> httpEntity, RestTemplate restTemplate) {
        try {
            ResponseEntity<byte[]> serverResponse = restTemplate.exchange(uri, method, httpEntity, byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
            logger.info(serverResponse);
            return serverResponse;
        } catch (HttpStatusCodeException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        }
    }
}