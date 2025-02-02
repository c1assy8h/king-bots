package com.knob.matchingsystem.service.impl.utils;


import com.knob.matchingsystem.config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingPool extends Thread{
    private static List<Player> players = new ArrayList<Player>(); //多个线程共用&读写冲突
    private final ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;
    private final static String startGameUrl = "http://localhost:3000/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    public  void addPlayer(Integer userId, Integer rating, Integer botId){
        lock.lock();
        try {
            players.add(new Player(userId, rating, botId, 0));
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId){
        lock.lock();
        try {
            List<Player> newPlayers = new ArrayList<>();
            for(Player player : players){
                if(!player.getUserId().equals(userId)){
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime() { //将所有玩家的等待时间+1
        for(Player player : players){
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    private boolean checkMatched(Player a, Player b){ //判断两位玩家是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10; //既满足a也满足b
    }

    private void sendResult(Player a, Player b){ //返回a b是否匹配
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", String.valueOf(a.getUserId()));
        data.add("a_bot_id", String.valueOf(a.getBotId()));
        data.add("b_id", String.valueOf(b.getUserId()));
        data.add("b_bot_id", String.valueOf(b.getBotId()));
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    private void matchPlayers(){ //尝试匹配所有玩家
        boolean[] used = new boolean[players.size()];
        for(int i = 0; i < players.size(); i++){
            if(used[i])  continue;
            for(int j = i + 1; j < players.size(); j++){
                if(used[j])  continue;
                Player a = players.get(i);
                Player b = players.get(j);
                if(checkMatched(a,b)) {
                    used[i] = true;
                    used[j] = true;
                    sendResult(a,b);
                    break;
                }
            }
        }
        List<Player> newPlayers = new ArrayList<>();
        for(int i = 0; i < players.size(); i++){
            if(!used[i]) newPlayers.add(players.get(i));
        }
        players = newPlayers;
    }
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    increaseWaitingTime(); //这两个函数都会操作players
                    matchPlayers();
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace(); // output error
                break;
            }
        }
    }
}
