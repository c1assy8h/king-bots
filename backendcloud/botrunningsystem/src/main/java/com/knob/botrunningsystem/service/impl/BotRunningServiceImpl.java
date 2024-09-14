package com.knob.botrunningsystem.service.impl;

import com.knob.botrunningsystem.service.BotRunningService;
import com.knob.botrunningsystem.service.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    public final static BotPool botPool = new BotPool();

    @Override
    public String addBot(Integer usrId, String botCode, String input) {
        System.out.println(usrId + ":" + botCode + ":" + input);
        botPool.addBot(usrId, botCode, input);
        return "add bot success";
    }
}
