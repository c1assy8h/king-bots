package com.knob.backend.controller.pk;

import com.knob.backend.service.pk.ReceiveBotMoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ReceiveBotMoveController {
    @Autowired
    private ReceiveBotMoveService receiveBotMoveService;

    @PostMapping("/pk/receive/bot/move/")
    public void receiveBotMove(@RequestParam MultiValueMap<String, String> params) {
        Integer useId = Integer.parseInt(Objects.requireNonNull(params.getFirst("useId")));
        Integer direction = Integer.parseInt(Objects.requireNonNull(params.getFirst("direction")));
        receiveBotMoveService.receiveBotMove(useId, direction);
    }
}
