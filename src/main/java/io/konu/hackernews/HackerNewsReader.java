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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by matt on 2/28/16.
 */
public class HackerNewsReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HackerNewsReader.class);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final Firebase hackerNewsRef;
    private final Firebase itemRef;
    private final Firebase topStoriesRef;

    public HackerNewsReader(final Firebase hackerNewsRef) {
        this.hackerNewsRef = hackerNewsRef;

        itemRef = hackerNewsRef.child("item");
        topStoriesRef = hackerNewsRef.child("topstories");
    }

    public void getTopStories(final Consumer<Map<String, Object>> storyConsumer) {
        try {
            final int limit = 10;
            final CountDownLatch latch = new CountDownLatch(limit);
            topStoriesRef.limitToFirst(limit).addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(final DataSnapshot snapshot) {
                    LOGGER.info("Received stories from Firebase {}", snapshot.getValue());
                    List<Long> storyIds = (List<Long>) snapshot.getValue();
                    for (long storyId : storyIds) {
                        getStory(latch, storyId, storyConsumer);
                    }
                }

                public void onCancelled(final FirebaseError error) {
                    HackerNewsReader.this.onCancelled(error);
                }
            });
            latch.await();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void getStory(final CountDownLatch latch, final long storyId, final Consumer<Map<String, Object>> storyConsumer) {
        itemRef.child(Long.toString(storyId)).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(final DataSnapshot snapshot) {
                Map<String, Object> story = (Map<String, Object>) snapshot.getValue();
                LOGGER.info("Received story from Firebase {}", story);
                storyConsumer.accept(story);
                latch.countDown();
            }

            public void onCancelled(final FirebaseError error) {
                HackerNewsReader.this.onCancelled(error);
            }
        });
    }

    private void onCancelled(final FirebaseError error) {
        LOGGER.info("Received cancellation from Firebase {}", error);
    }

    public static void main(final String[] args) {
        try {
            final Firebase hackerNewsRef = new Firebase("https://hacker-news.firebaseio.com/v0");
            final HackerNewsReader reader = new HackerNewsReader(hackerNewsRef);
            reader.getTopStories(story -> {
                LOGGER.info("Main received story from Firebase {}", story);
            });
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
