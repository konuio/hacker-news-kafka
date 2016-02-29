package io.konu.hackernews;

import com.firebase.client.Firebase;
import com.google.common.base.Throwables;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by matt on 2/28/16.
 */
public class HackerNewsProducer {
    private static Logger LOGGER = LoggerFactory.getLogger(HackerNewsProducer.class);

    private Producer<String, String> producer;

    public HackerNewsProducer(Producer<String, String> producer) {
        this.producer = producer;
    }

    public void sendStory(Map<String, Object> story) {
        producer.send(new ProducerRecord<>("test", "message", (String) story.get("title")));
    }

    public static void main(String[] args) {
        try {
            Firebase hackerNewsRef = new Firebase("https://hacker-news.firebaseio.com/v0");
            HackerNewsReader hackerNewsReader = new HackerNewsReader(hackerNewsRef);

            Properties properties = new Properties();
            properties.put("bootstrap.servers", "localhost:9091");
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
                HackerNewsProducer hackerNewsProducer = new HackerNewsProducer(producer);
                hackerNewsReader.getTopStories(hackerNewsProducer::sendStory);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
