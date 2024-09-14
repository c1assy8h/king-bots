package com.knob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.knob.backend.consumer.WebSocketServer;
import com.knob.backend.pojo.Bot;
import com.knob.backend.pojo.Record;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ThemeResolver;
import com.knob.backend.pojo.User;
import java.nio.file.WatchEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;

    private final int[][] map;
    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, -1, 0, 1};

    private final Player playerA, playerB;
    private Integer nextStepA = null; // 0123表示上下左右四个方向
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock(); // 互斥，解决对同一个数据nextStepA的读写问题
    private String status = "playing"; // playing -> finished
    private String loser = ""; // all: 平局
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";


    public Game(
            Integer rows,
            Integer cols,
            Integer inner_walls_count,
            Integer idA,
            Bot botA,
            Integer idB,
            Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.map = new int[rows][cols];

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";
        if(botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if(botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }

        playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, botIdB, botCodeB,1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }
    public Player getPlayerB() {
        return playerB;
    }
    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }

    }
    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }


    public int[][] getMap() {
        return map;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if(sx == tx && sy == ty) return true;
        map[sx][sy] = 1;

        for(int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if(x < 0 || x >= this.rows || y < 0 || y >= this.cols) continue;
            if(check_connectivity(x, y, tx, ty)) {
                map[sx][sy] = 0;
                return true;
            }
        }

        map[sx][sy] = 0;
        return false;
    }
    private boolean draw() {
        for(int i = 0; i < this.rows; i++) {
            for(int j = 0; j < this.cols; j++) {
                map[i][j] = 0; //0是空地1是墙
            }
        }

        Random rand = new Random();
        for(int i = 0; i < this.rows; i++)
            map[i][0] = map[i][this.cols - 1] = 1;
        for(int i = 0; i < this.cols; i++)
            map[0][i] = map[this.rows - 1][i] = 1;
        for(int i = 0; i < this.inner_walls_count / 2; i++) {
            for(int j = 0; j < 1000; j++) {
                int r = rand.nextInt(this.rows);
                int c = rand.nextInt(this.cols);
                if(map[r][c] == 1 || map[this.rows - 1 - r][this.cols - 1 - c] == 1)
                    continue;
                if(r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
                    continue;

                map[r][c] = map[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }
        return check_connectivity(this.rows-2, 1, 1, this.cols -2);
    }

    public void createMap() {
        for(int i = 0; i < 1000; i++) {
            if(draw())
                break;
        }
    }

    private String getInput(Player player) { //将当前的局面信息 编码成字符串
        //map#自己的起始坐标#自己的操作#对手的起始坐标#对手的操作
        Player me, you;
        if(playerA.getId().equals(player.getBotId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" + me.getSy() + "#" +
                me.getStepsString() + "#" +
                you.getSx() + "#" + you.getSy() + "#"
                +you.getStepsString();
    }

    private void sendBotCode(Player player) {
        if(player.getBotId().equals(-1)) return; //人工操作不需要发送代码，bot的话不需要接收人的按键输入
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    private boolean nextStep() { // 等待两名玩家的下一步操作
        try {
            Thread.sleep(200); // 蛇走一格需要200毫秒，前端200ms才能画一格（如果在200ms中接收了两步，但前端只会展示最后一步）
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendBotCode(playerA);
        sendBotCode(playerB);

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        if(map[cell.x][cell.y] == 1) return false;// 蛇A的最后一位是否是墙

        for(int i = 0; i < n - 1; i++) {
            if(cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y) return false; //蛇身撞住蛇尾了
        }

        for(int i = 0; i < n - 1; i++) {
            if(cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y) return false;
        }

        return true;
    }

    private void judge() { // 判断
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if(!validA || !validB) {
            status = "finished";
            if(!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private void sendAllMessage(String message) {
        if(WebSocketServer.users.get(playerA.getId()) != null) {
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if(WebSocketServer.users.get(playerB.getId()) != null) {
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }

    private void sendMove() { // 向两个client传递移动信息，A需要收到B的移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }

    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void saveToDatabase() { //保存对战结果
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if("A".equals(loser)) {
            ratingA -= 2;
            ratingB += 5;
        } else {
            ratingA += 5;
            ratingB -= 2;
        }

        updateUserRating(playerA, ratingA);
        updateUserRating(playerB, ratingB);

        Record record = new Record(
                null, playerA.getId(), playerA.getSx(), playerA.getSy(),
                playerB.getId(), playerB.getSx(), playerB.getSy(), playerA.getStepsString(), playerB.getStepsString(),
                getMapString(), loser, new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() { // 向两个玩家的client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i ++ ) {
            if(nextStep()) { // 是否获取了两条蛇的下一步操作
                judge();
                if(status.equals("playing")) { // status非空
                    sendMove();
                } else { // 游戏结束
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                // 涉及nextStepA的读操作
                lock.lock();
                try {
                    if(nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if(nextStepA != null) {
                        loser = "A";
                    } else if(nextStepB != null) {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
    public  String getMapString() {
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                res.append(map[i][j]);
            }
        }
        return res.toString();
    }
}
