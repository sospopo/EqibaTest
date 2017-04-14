import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by chunmiao on 17-4-4.
 */
public class TestM {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient transport = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(transport);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        taskScheduler.initialize();
        stompClient.setTaskScheduler(taskScheduler); // for heartbeats

        String url = "ws://localhost:8080/portfolio";
        StompSessionHandler myHandler = new MyStompSessionHandler1();
        ListenableFuture<StompSession> ret = stompClient.connect(url, myHandler);
        ret.addCallback(new ListenableFutureCallback<StompSession>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(StompSession stompSession) {
                stompSession.subscribe("/talk/miao/message", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders stompHeaders) {
                        return TalkMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders stompHeaders, Object o) {
                        TalkMessage message = (TalkMessage) o;
                        System.out.println(message.fromUser + " : " + message.getContent());
                    }
                });

                stompSession.subscribe("/app/marco", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders stompHeaders) {
                        return TalkMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders stompHeaders, Object o) {
                        TalkMessage s = (TalkMessage) o;
                        System.out.println("subscribe message :");
                        System.out.println("connect success!");
                    }
                });
            }
        });

        StompSession sess = ret.get();
        sess.setAutoReceipt(true);
        Scanner in = new Scanner(System.in);
        String s = "";
        while (!s.equals("q")) {
            s = in.nextLine();
            TalkMessage message = new TalkMessage();
            message.setToUser("chun");
            message.setFromUser("miao");
            message.setContent(s);
            StompSession.Receiptable receiptable = sess.send("/app/message", message);
            receiptable.addReceiptLostTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("send a message succeed!");
                }
            });
        }
        in.close();


    }
}


class MyStompSessionHandler1 extends StompSessionHandlerAdapter {
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        Map<String, String> headers = connectedHeaders.toSingleValueMap();
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            System.out.println(kv.getKey() + " - " + kv.getValue());
        }
        System.out.println("connected session : "+session.getSessionId());
    }

}
