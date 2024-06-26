package io.github.oasis.engine;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class NatsRunner {

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            JetStream jetStream = nc.jetStream();

            executorService.submit(new NatConsumer(nc, "c-1", "test.1", latch));
            executorService.submit(new NatConsumer(nc, "c-2", "test.*", latch));
            executorService.shutdown();

            Thread.sleep(1000);

            nc.publish("test.1", "hello world 1!".getBytes(StandardCharsets.UTF_8));
            nc.publish("test.2", "hello world 2!".getBytes(StandardCharsets.UTF_8));
            nc.publish("test.1", "hello world 3!".getBytes(StandardCharsets.UTF_8));
            nc.flush(Duration.ofSeconds(2));

            latch.await();
        }
    }

    private static class NatConsumer implements Runnable {

        private final Connection nc;
        private final String consumerId;
        private final CountDownLatch latch;
        private final String subject;

        private NatConsumer(Connection nc, String cId, String subj, CountDownLatch latch) {
            this.nc = nc;
            this.consumerId = cId;
            this.latch = latch;
            this.subject = subj;
        }

        @Override
        public void run() {
            System.out.println("Running consumer... " + consumerId);
            Subscription sub = nc.subscribe(subject);
            while (true) {
                try {
                    System.out.println("Reading message...");
                    Message message = sub.nextMessage(Duration.ofSeconds(3));
                    var content = new String(message.getData(), StandardCharsets.UTF_8);
                    System.out.printf("[%s] Received: %s\n", consumerId, content);
                    latch.countDown();

                    if ("kill".equals(content)) {
                        System.out.println("Shutting down consumer...." + consumerId);
                        break;
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            sub.unsubscribe();
        }
    }
}
