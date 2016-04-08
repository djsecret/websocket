package com.damon.websocket.demo;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 一个简单的WebSocket服务器例子
 * onMessage方法处理每一条从客户端拿到的消息
 * 一个定时任务每秒执行一次,向客户端广播消息
 * Created by dongjun.wei on 16/4/8.
 */
@ServerEndpoint(value = "/ws/{param}")
public class WebSocketServer {

    private static final Map<Session, Session> sessionMap = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    static {
        executorService.scheduleAtFixedRate(new TimerTask(), 1, 1, TimeUnit.SECONDS);
    }

    @OnOpen
    public void onOpen(Session session) {
        sessionMap.put(session, session);
        System.out.println("---onOpen");
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("param") String param) {
        System.out.println("onMessage：" + message);
        try {
            session.getBasicRemote().sendText("Hello " + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose");
        sessionMap.remove(session);
    }

    @OnError
    public void onError(Throwable cause) {
        System.out.println("error");
        cause.printStackTrace();
    }

    private static final class TimerTask implements Runnable {

        private int counter = 1;

        @Override
        public void run() {
            System.out.println("run:" + counter);
            String message = "来自服务端的消息，当前时间是：" + new Date();
            counter++;

            if (!sessionMap.isEmpty()) {
                for (Session session : sessionMap.keySet()) {
                    session.getAsyncRemote().sendText(message);

                    if (counter > 100) {
                        try {
                            session.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
