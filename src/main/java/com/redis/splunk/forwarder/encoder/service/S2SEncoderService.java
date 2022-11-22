package com.redis.splunk.forwarder.encoder.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S2SEncoderService {

    private final static Logger logger = LogManager.getLogger(S2SEncoderService.class);

    public InputStream encode(String body) throws IOException {
        // Proprietary Code - From Splunk
        logger.info("encoding s2s payload");
        return null;
    }
}
