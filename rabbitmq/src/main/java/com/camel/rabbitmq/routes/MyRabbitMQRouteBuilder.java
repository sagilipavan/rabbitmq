package com.camel.rabbitmq.routes;

import com.camel.rabbitmq.bean.MyBean;
import com.camel.rabbitmq.processors.YamlProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.YAMLDataFormat;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class MyRabbitMQRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        ObjectMapper objectMapper =new ObjectMapper();
        YAMLDataFormat yaml = new YAMLDataFormat();
        from("file:src/mq?noop=true").log("This file extension is ${file:name}   of  ${file:ext}")
                .choice()
                .when(simple("${file:ext} == 'xml'"))
                .unmarshal().jacksonXml()
                .process(exchange -> exchange.getIn().setBody((HashMap<String, Object>) exchange.getIn().getBody()))
                .marshal(yaml)
                .to("rabbitmq://localhost:5672/xml?queue=xml&autoDelete=false")
                .when(simple("${file:ext} == 'json'"))
                .setBody(body())
                .process(exchange -> exchange.getIn().setBody(objectMapper.writeValueAsString(exchange.getIn().getBody())))
                .to("rabbitmq://localhost:5672/json?queue=json&autoDelete=false")
                .when(simple("${file:ext} == 'csv'"))
                .unmarshal().csv()
                .process(exchange -> exchange.getIn().setBody((List<List<String>>) exchange.getIn().getBody()))
                .to("rabbitmq://localhost:5672/csv?queue=csv&autoDelete=false")
                .otherwise()
                .to("rabbitmq://localhost:5672/deadleatter?queue=deadleatter&autoDelete=false").end();

        from("rabbitmq://localhost:5672/json?queue=json&autoDelete=false")
                .doTry()
                .log("Got from RabbitMQ ${body}")
                .unmarshal(new JacksonDataFormat(MyBean.class))
                //.bean(MyBean.class)
//                .setBody(exchange -> exchange.getIn().getBody())
//              .bean(new FileTransferProcessor(), "parse")
                .process(new YamlProcessor())
                .to("file:src/target/output/json?fileName=${file:name}_${date:now:yyyyMMddHHmmssSSS}.yml")
                .doCatch(Exception.class)
                .log("Error while processing json")
                .doFinally()
                .to("file:src/target/output?fileName=${file:name}_${date:now:yyyyMMddHHmmssSSS}_error.yml");
        from("rabbitmq://localhost:5672/csv?queue=csv&autoDelete=false")
                .doTry()
                .log("Got from RabbitMQ ${body}")
                .unmarshal(new JacksonDataFormat(MyBean.class))
                .process(new YamlProcessor())
                .to("file:src/target/output/csv?fileName=${file:name}_${date:now:yyyyMMddHHmmssSSS}.yml")
                .doCatch(Exception.class)
                .log("Error while processing csv")
                .doFinally()
                .to("file:src/target/output?fileName=${file:name}_${date:now:yyyyMMddHHmmssSSS}_error.yml");
        from("rabbitmq://localhost:5672/xml?queue=xml&autoDelete=false")
                .doTry()
                .log("Got from RabbitMQ ${body}")
                .unmarshal(new JacksonDataFormat(MyBean.class))
                .process(new YamlProcessor())
                .to("file:src/target/output/xml?fileName=${file:name.noext}_${date:now:yyyyMMddHHmmssSSS}.yml")
                .doCatch(Exception.class)
                .log("Error while processing xml")
                .doFinally()
                .to("file:src/target/output?fileName=${file:name.noext}_${date:now:yyyyMMddHHmmssSSS}_error.yml");


    }
}

