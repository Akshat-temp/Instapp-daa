package algorithm;

import models.Post;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;


public class FeedRanker {

    private static final Comparator<Post> BY_LIKES_THEN_RECENCY = (a, b) -> {
        int diff = b.getLikes() - a.getLikes();
        if (diff != 0) return diff;
        return Long.compare(b.getTimestamp(), a.getTimestamp());
    };

    
    public List<Post> getRankedFeed(List<Post> posts) {
        return drain(posts);
    }

    
    public List<Post> getFollowingFeed(List<Post> allPosts, Set<String> following) {
        List<Post> filtered = new ArrayList<>();
        for (Post p : allPosts)
            if (following.contains(p.getAuthorUsername()))
                filtered.add(p);
        return drain(filtered);
    }

   
    private List<Post> drain(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return new ArrayList<>();
        PriorityQueue<Post> heap = new PriorityQueue<>(
                Math.max(1, posts.size()), BY_LIKES_THEN_RECENCY);
        heap.addAll(posts);
        List<Post> result = new ArrayList<>(posts.size());
        while (!heap.isEmpty()) result.add(heap.poll());
        return result;
    }
}
