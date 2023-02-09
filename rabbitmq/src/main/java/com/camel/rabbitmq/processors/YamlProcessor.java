package com.camel.rabbitmq.processors;

import com.camel.rabbitmq.bean.MyBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class YamlProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        MyBean currencyExchangeDto = exchange.getIn().getBody(MyBean.class);
        System.out.println("CSV-Processor complete");
        String yamlString = convertToYaml(currencyExchangeDto);
        exchange.getIn().setBody(yamlString);
    }

    public static String convertToYaml(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        String yaml = mapper.writeValueAsString(obj);
        System.out.println("yaml : " +yaml);
        return yaml;
    }
}
