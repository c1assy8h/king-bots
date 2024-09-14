package com.knob.matchingsystem.service.impl;

import com.knob.matchingsystem.service.MatchingService;
import com.knob.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
    public final static MatchingPool matchingPool = new MatchingPool(); //匹配系统全局就一个线程,每次对局就一个线程

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add player: " + userId + " rating: " + rating);
        matchingPool.addPlayer(userId, rating, botId);
        return "add success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove player: " + userId);
        matchingPool.removePlayer(userId);
        return "remove success";
    }
}
