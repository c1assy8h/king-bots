package com.knob.matchingsystem.controller;

import com.knob.matchingsystem.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class MatchingController {
    @Autowired
    private MatchingService matchingService;

    @PostMapping("/player/add/")
    public String addPlayer(@RequestParam MultiValueMap<String, String> map) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(map.getFirst("user_id")));
        Integer rating = Integer.parseInt(Objects.requireNonNull(map.getFirst("rating")));
        Integer botId = Integer.parseInt(Objects.requireNonNull(map.getFirst("bot_id")));
        return matchingService.addPlayer(userId, rating);
    }

    @PostMapping("/player/remove/")
    public String removePlayer(@RequestParam MultiValueMap<String, String> map) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(map.getFirst("userId")));
        return matchingService.removePlayer(userId);
    }


}
