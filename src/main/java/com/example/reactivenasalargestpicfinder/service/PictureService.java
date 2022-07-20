package com.example.reactivenasalargestpicfinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class PictureService {

    @Value("${nasa.api.key}")
    private String apiKey;

    @Value("${nasa.base.url}")
    private String baseUrl;


    public Mono<String> findLargestPicUrl(int sol) {
        return WebClient.create(baseUrl)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("api_key", apiKey)
                        .queryParam("sol", sol)
                        .build())
                .exchangeToMono(resp -> resp.bodyToMono(JsonNode.class))
                .map(json -> json.get("photos"))
                .flatMapMany(Flux::fromIterable)
                .map(json -> json.get("img_src"))
                .map(JsonNode::asText)
                .flatMap(url -> WebClient
                        .create(url)
                        .head()
                        .exchangeToMono(ClientResponse::toBodilessEntity)
                        .map(HttpEntity::getHeaders)
                        .map(HttpHeaders::getLocation)
                        .map(URI::toString)
                        .flatMap(realUrl -> WebClient
                                .create(realUrl)
                                .head()
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(HttpEntity::getHeaders)
                                .map(HttpHeaders::getContentLength)
                                .map(size -> new Picture(url, size))))
                .reduce((p1, p2) -> p1.size > p2.size ? p1 : p2)
                .map(Picture::url);
    }

    record Picture(String url, Long size) {}
}
