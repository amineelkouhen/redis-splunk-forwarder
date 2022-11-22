# Redis-Splunk Forwarder
Splunk Event Forwarder from Redis

## Usage
Clone the Repository:
```bash
git clone https://github.com/amineelkouhen/redis-splunk-forwarder.git
```

There is a docker compose script which will bootstrap all the components required to make this demo work.

1. Run `docker-compose up` from the root dir
2. The containers will start in the correct order
3. On startup:
- The Splunk Service will bootstrap `Splunk Enterprise`. After the container starts up successfully and enters the "healthy" state, you should be able to access SplunkWeb at [http://localhost:8000](http://localhost:8000) with admin:Admin12345.
- The Forwarder Service will bootstrap the Splunk-Redis Forwarder Module that listens to the `forward` stream and consume events.

```bash
Creating splunk-service ... done
Creating forwarder-service ... done
Attaching to splunk-service, forwarder-service
forwarder-service | [main] c.r.s.f.configuration.JedisConfig        : host redis - port 6379
forwarder-service | [main] c.r.s.f.consumer.service.ConsumeService  : consuming object with ID 5f5fffee-3ed2-4b66-98ed-b596ef9f3a4e
```
- The Forwarder Module will re-create a Rest Request based on data received in the `forward` stream (and the headers embded on events) and will re-send the event to the HttpEventCollector (HEC) of Splunk.
```bash
forwarder-service | [main] c.r.s.f.proxy.service.HECProxyService    : <200,{"text":"Success","code":0},[Date:"Tue, 22 Nov 2022 21:23:16 GMT", Content-Type:"application/json; charset=UTF-8", X-Content-Type-Options:"nosniff", Content-Length:"27", Vary:"Authorization", Connection:"Keep-Alive", X-Frame-Options:"SAMEORIGIN", Server:"Splunkd"]>
```
### Software Reqs
- Docker
- Java 17+
