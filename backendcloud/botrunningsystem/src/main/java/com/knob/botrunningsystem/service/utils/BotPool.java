package com.knob.botrunningsystem.service.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends  Thread{
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();

    public void addBot(Integer userId, String botCode, String input){
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll(); //唤醒所有线程
        } finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot) { //这里只支持java,安全性的话加沙箱
        //采用线程，用户写了死循环的话，会 自动断开
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot); //两个bot最多等待5秒

    }

    @Override
    public void run() {
        while (true) {
            lock.lock(); // 生产者消费者对线程操作冲突
            if(bots.isEmpty()) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove(); //把队头取出来，解锁
                lock.unlock(); //解决队列的读写冲突问题

                consume(bot); // 比较耗时，可能执行几秒钟，编译执行java代码
            }
        }
    }
}
