package it.gov.pagopa.authorizer.config.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {

  @Bean(name = "apiconfigSelfcareClient")
  public WebClient apiconfigSelfcareClient(@Value("${client.apiconfig-selfcare.hostname}") String hostname,
      @Value("${client.common.readTimeout}") int timeout,
      @Value("${client.common.connectionTimeout}") int connTimeout,
      @Value("${client.apiconfig-selfcare.subscriptionKey}") String subscriptionKey) {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout)
        .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));
    log.debug(String.format("Defining a web-client pointing towards APIConfig-Selfcare Integration service. Base URL: [%s].", hostname));
    return WebClient.builder()
        .baseUrl(hostname)
        .defaultHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
        .defaultHeader("Content-Type", "application/json")
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(this::configureClientCodec)
            .build())
        .build();
  }

  private ClientCodecConfigurer configureClientCodec(ClientCodecConfigurer clientCodecConfigurer) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    clientCodecConfigurer.registerDefaults(false);
    clientCodecConfigurer.customCodecs().register(StringDecoder.allMimeTypes());
    clientCodecConfigurer.customCodecs().register(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
    clientCodecConfigurer.customCodecs().register(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
    return clientCodecConfigurer;
  }

}
