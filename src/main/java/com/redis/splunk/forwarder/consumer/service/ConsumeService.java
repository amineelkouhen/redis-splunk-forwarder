package com.redis.splunk.forwarder.consumer.service;

import com.redis.splunk.forwarder.transformer.service.TransformService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;

import java.net.SocketException;
import java.util.*;

@Service
public class ConsumeService {
    @Autowired
    TransformService transformService;
    private final static Logger logger = LogManager.getLogger(ConsumeService.class);

    public void consume(UnifiedJedis client, String streamName, String groupName, String forwarderName) {
        try {
            Map<String, StreamEntryID> stream = Map.of(streamName, StreamEntryID.UNRECEIVED_ENTRY);
            List<Map.Entry<String, List<StreamEntry>>> response =
                    client.xreadGroup(groupName, forwarderName, XReadGroupParams.xReadGroupParams().count(1).block(0).noAck(), stream);

            if(response != null){
                for(Map.Entry<String, List<StreamEntry>> entry : response){
                    for (StreamEntry streamEntry: entry.getValue()) {
                        String objectID = streamEntry.getFields().get("Object_ID");
                        logger.info("consuming object with ID {}", objectID);
                        client.xack(streamName, groupName, streamEntry.getID());
                        JSONObject payload = new JSONObject(client.jsonGetAsPlainString(objectID, Path.ROOT_PATH));
                        transformService.extractRequestFromEvent(payload);
                    }
                }
            }
        }
        catch (JedisDataException | SocketException e1){}
        catch (Exception e2) {
            logger.error(e2.getMessage());
        }
    }
}
