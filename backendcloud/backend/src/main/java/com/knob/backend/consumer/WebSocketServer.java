package com.knob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.knob.backend.consumer.utils.Game;
import com.knob.backend.consumer.utils.JwtAuthentication;
import com.knob.backend.mapper.BotMapper;
import com.knob.backend.mapper.RecordMapper;
import com.knob.backend.mapper.UserMapper;
import com.knob.backend.pojo.Bot;
import com.knob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


//import jakarta.websocket.OnClose;
//import jakarta.websocket.OnMessage;
//import jakarta.websocket.OnOpen;
//
//import jakarta.websocket.Session;
//import jakarta.websocket.server.PathParam;
//import jakarta.websocket.server.ServerEndpoint;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    // final private static CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>(); //匹配池 线程安全
    private User user;
    private Session session = null;

    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate;
    private static BotMapper botMapper;

    public Game game = null;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }
    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("Connected to websocket");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if(this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }


    }

    @OnClose
    public void onClose() {
        System.out.println("Disconnected from websocket");
        if(this.user != null) {
            users.remove(this.user.getId());
            // matchPool.remove(this.user);
        }
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User userA = userMapper.selectById(aId);
        User userB = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId);
        Bot botB = botMapper.selectById(bBotId);

        Game game = new Game(
                13,
                14,
                20,
                userA.getId(),
                botA,
                userB.getId(),
                botB);
        game.createMap();
        if(users.get(userA.getId()) != null) {
            users.get(userA.getId()).game = game;
        }
        if(users.get(userB.getId()) != null) {
            users.get(userB.getId()).game = game;
        }
        game.start(); // Thread 每开一局每一个game是一个线程

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx()); //位置的横纵坐标
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getMap());


        //将配对的信息回传
        JSONObject response1 = new JSONObject();
        response1.put("event", "start-matching");
        response1.put("opponent", userB.getUsername());
        response1.put("photo", userB.getPhoto());
        response1.put("game", respGame);
        if(users.get(userA.getId()) != null) {
            users.get(userA.getId()).sendMessage(response1.toJSONString());
        }


        JSONObject response2 = new JSONObject();
        response2.put("event", "start-matching");
        response2.put("opponent", userA.getUsername());
        response2.put("photo", userA.getPhoto());
        response2.put("game", game.getMap());
        if(users.get(userB.getId()) != null) {
            users.get(userB.getId()).sendMessage(response2.toJSONString());
        }

    }

    private void startMatching(Integer botId) { //多线程
        System.out.println("Start matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", String.valueOf(this.user.getId()));
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);

    }

    private void stopMatching() {
        System.out.println("stopMatching");
        // matchPool.remove(this.user);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", String.valueOf(this.user.getId()));
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction) {
        if (game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId().equals(-1)) //-1表示人工，使用按键自己玩，而不是用ai
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId().equals(-1))
                game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) { //路由 把任务转接给谁
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if("start-matching".equals(event)) { //event != null && event.equals("start-matching")
            startMatching(data.getInteger("bot_id"));
        } else if("stop-matching".equals(event)) {
            stopMatching();
        } else if("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
