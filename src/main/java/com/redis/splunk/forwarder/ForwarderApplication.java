package com.redis.splunk.forwarder;

import com.redis.splunk.forwarder.configuration.JedisConfig;
import com.redis.splunk.forwarder.consumer.service.ConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import redis.clients.jedis.UnifiedJedis;

import java.util.UUID;

@SpringBootApplication
@ComponentScan
public class ForwarderApplication implements ApplicationRunner {
	@Autowired
	ConsumeService consumeService;
	@Autowired
    JedisConfig jedisConfig;
	@Value("${stream.name}")
	String STREAM_NAME = "forward";
	@Value("${stream.group}")
	String GROUP_NAME = "splunk-app-group";
	@Value("${stream.wait}")
	Integer wait = 10;
	final static String FORWARDER_NAME = "forwarder-" + UUID.randomUUID();

	public static void main(String[] args) {
		SpringApplication.run(ForwarderApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments arg0) throws Exception {
		UnifiedJedis client = jedisConfig.getClient();
		while(true) {
			consumeService.consume(client, STREAM_NAME, GROUP_NAME, FORWARDER_NAME);
			Thread.sleep(wait * 1000);
		}
	}
}
