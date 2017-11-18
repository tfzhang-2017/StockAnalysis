package com.stock.ztf.StockAnalysis.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig{
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }
    
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setReadTimeout(60*1000);//ms
//        factory.setConnectTimeout(60*1000);//ms
//        SocketAddress address = new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort());
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
//        factory.setProxy(proxy);
        return factory;
    }
}
