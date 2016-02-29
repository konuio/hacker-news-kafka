package io.konu.hackernews;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Created by matt on 2/28/16.
 */
public class HackerNewsReader {
    private static Logger LOGGER = LoggerFactory.getLogger(HackerNewsReader.class);

    private Firebase hackerNewsRef;
    private Firebase itemRef;
    private Firebase topStoriesRef;

    public HackerNewsReader(Firebase hackerNewsRef) {
        this.hackerNewsRef = hackerNewsRef;

        itemRef = hackerNewsRef.child("item");
        topStoriesRef = hackerNewsRef.child("topstories");
    }

    public void getTopStories(Consumer<Map<String, Object>> storyConsumer) {
        try {
            int limit = 10;
            CountDownLatch latch = new CountDownLatch(limit);
            topStoriesRef.limitToFirst(limit).addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    LOGGER.info("Received stories from Firebase {}", snapshot.getValue());
                    List<Long> storyIds = (List<Long>) snapshot.getValue();
                    for (long storyId : storyIds) {
                        getStory(latch, storyId, storyConsumer);
                    }
                }

                public void onCancelled(FirebaseError error) {
                    HackerNewsReader.this.onCancelled(error);
                }
            });
            latch.await();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void getStory(CountDownLatch latch, long storyId, Consumer<Map<String, Object>> storyConsumer) {
        itemRef.child(Long.toString(storyId)).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> story = (Map<String, Object>) snapshot.getValue();
                LOGGER.info("Received story from Firebase {}", story);
                storyConsumer.accept(story);
                latch.countDown();
            }

            public void onCancelled(FirebaseError error) {
                HackerNewsReader.this.onCancelled(error);
            }
        });
    }

    private void onCancelled(FirebaseError error) {
        LOGGER.info("Received cancellation from Firebase {}", error);
    }

    public static void main(String[] args) {
        try {
            Firebase hackerNewsRef = new Firebase("https://hacker-news.firebaseio.com/v0");
            HackerNewsReader reader = new HackerNewsReader(hackerNewsRef);
            reader.getTopStories(story -> {
                LOGGER.info("Main received story from Firebase {}", story);
            });
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
