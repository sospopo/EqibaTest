import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by chunmiao on 17-4-4.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Main {
        public static void main(String[] args) {
            final CountDownLatch latch = new CountDownLatch(100);

            List<Transport> transports = new ArrayList<>(1);
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            WebSocketClient transport = new SockJsClient(transports);
            WebSocketStompClient stompClient = new WebSocketStompClient(transport);
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.afterPropertiesSet();
            stompClient.setTaskScheduler(taskScheduler); // for heartbeats

            String url = "ws://localhost:8080/portfolio";
            StompSessionHandler myHandler = new MyStompSessionHandler(latch);
            stompClient.connect(url, myHandler);
            try {
                latch.await(3, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class MyStompSessionHandler extends StompSessionHandlerAdapter {
        private CountDownLatch latch;

        public MyStompSessionHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void afterConnected(StompSession session,
                                   StompHeaders connectedHeaders) {
            System.out.println("StompHeaders: " + connectedHeaders.toString());
            session.subscribe("/topic/getResponse", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return TalkMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    TalkMessage greeting = (TalkMessage) payload;
                    System.out.println(greeting);
                    latch.countDown();
                    session.disconnect();
                }

            });
            session.send("/app/welcome", new TalkMessage("Dave"));
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println(exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session,
                                         Throwable exception) {
            System.out.println("transport error.");
        }
    }
