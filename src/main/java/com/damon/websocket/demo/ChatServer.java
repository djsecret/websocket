package com.damon.websocket.demo;

import com.google.common.html.HtmlEscapers;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用websocket建立长连接,实现一个简易的聊天室
 * Created by dongjun.wei on 16/4/8.
 */
@ServerEndpoint("/ws/chat")
public class ChatServer {

    private static final String GUEST_PREFIX = "Guest";

    private static final AtomicInteger connectionId = new AtomicInteger(0);

    private final String nickname;

    public ChatServer() {
        nickname = GUEST_PREFIX + connectionId.getAndIncrement();
    }

    //private Session session;

    //private static final Set<ChatServer> connections = new CopyOnWriteArraySet<>();

    private static final Map<Session, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        //this.session = session;
        //connections.add(this);
        sessionMap.put(session, session);
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) {
        //connections.remove(this);
        sessionMap.remove(session);
        String message = String.format("* %s %s",
                nickname, "has disconnected.");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(String message) {
        String filteredMessage = String.format("%s: %s",
                nickname, HtmlEscapers.htmlEscaper().escape(message));
        System.out.println(filteredMessage);
        broadcast(filteredMessage);
    }

    @OnError
    public void onError(Throwable throwable) {
        System.out.println("onError");
        throwable.printStackTrace();
    }

    private static void broadcast(String message) {
//        for (ChatServer chatServer : connections) {
//            try {
//                synchronized (chatServer) {
//                    chatServer.session.getBasicRemote().sendText(message);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                connections.remove(chatServer);
//
//                try {
//                    chatServer.session.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//
//                String msg = String.format("* %s %s",
//                        chatServer.nickname, "has been disconnected.");
//                broadcast(msg);
//            }
//        }
        if (!sessionMap.isEmpty()) {
            for (Session session : sessionMap.keySet()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
