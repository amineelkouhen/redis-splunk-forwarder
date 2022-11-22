package com.redis.splunk.forwarder.transformer.service;

import com.redis.splunk.forwarder.proxy.service.ProxyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

@Service
public class TransformService {
    @Autowired
    ProxyService proxyService;
    @Value("${proxy.forward.raw}")
    boolean forwardRawData = true;

    public void extractRequestFromEvent(JSONObject payload) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException {
        JSONObject event = payload.getJSONObject("event");

        String requestUrl = event.getString("path");
        if(forwardRawData)
            event.remove("path");

        HttpMethod method = HttpMethod.resolve(event.getString("method"));
        if(forwardRawData)
            event.remove("method");

        HttpHeaders headers = new HttpHeaders();
        JSONObject headersObject = event.getJSONObject("headers");
        Iterator<String> headerNames = headersObject.keys();
        while (headerNames.hasNext()) {
            String headerName = headerNames.next();
            headers.set(headerName, headersObject.getString(headerName));
        }
        if(forwardRawData)
            event.remove("headers");

        proxyService.processProxyRequest(requestUrl, method, headers, payload);
    }
}
