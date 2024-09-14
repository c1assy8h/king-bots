package com.knob.botrunningsystem.service.utils;

import com.knob.botrunningsystem.config.RestTemplateConfig;
import com.knob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot){ //timeout最多执行多长时间
        this.bot = bot; //存储bot
        this.start(); //开一个新线程执行run();当前线程执行join

        try {
            this.join(timeout); //等start()也就是run()执行完，继续执行。或者等待时间已经达到了timeout秒，继续执行其后面的操作
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt(); //最多等待timeout秒，就 中断当前线程。防止用户写了死循环
        }
    }

    private String addUid(String code, String uid){ //在code中的Bot类名后添加uid
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0,k) + uid + code.substring(k);
    }

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID(); //因为同一个bot可能修改AI代码，但是botId一直不变，就不会重新编译代码。所以加入随机
        String uid = uuid.toString().substring(0,8);

        Supplier<Integer> botInterface = Reflect.compile(
                "com.knob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid)
        ).create().get();

        File file = new File("input.txt");
        try {
            PrintWriter fout = new PrintWriter(file);
            fout.println(bot.getInput());
            fout.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Integer direction = botInterface.get();
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());
        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
