package com.example.reactivenasalargestpicfinder.controller;

import com.example.reactivenasalargestpicfinder.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("pictures")
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @GetMapping(value = "{sol}/largest", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> largestPicture(@PathVariable Integer sol) {
        return pictureService.findLargestPicUrl(sol)
                .flatMap(url -> WebClient
                        .create(url)
                        .mutate()
                        .codecs(conf -> conf
                                .defaultCodecs()
                                .maxInMemorySize(10_000_000))
                        .build()
                        .get()
                        .exchangeToMono(resp -> resp.bodyToMono(byte[].class)));
    }
}
