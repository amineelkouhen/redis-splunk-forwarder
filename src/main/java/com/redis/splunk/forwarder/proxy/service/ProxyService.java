package com.redis.splunk.forwarder.proxy.service;

import com.redis.splunk.forwarder.encoder.service.S2SEncoderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Service
public class ProxyService {

    @Autowired
    S2SProxyService s2sProxyService;
    @Autowired
    HECProxyService hecProxyService;
    @Autowired
    S2SEncoderService encodeService;

    private final static Logger logger = LogManager.getLogger(ProxyService.class);
    final static String S2S = "/services/collector/s2s";

    public ResponseEntity processProxyRequest(String requestUrl, HttpMethod method, HttpHeaders headers, JSONObject payload) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
        if (requestUrl.equals(S2S)) {
            InputStream encoded = encodeService.encode(payload.toString());
            return s2sProxyService.processProxyRequest(requestUrl, method, headers, encoded);
        } else {
            return hecProxyService.processProxyRequest(requestUrl, method, headers, payload.toString());
        }
    }
}
