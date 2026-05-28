package algorithm;

import models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class TrendingTracker {

   
    public List<Post> getTopK(List<Post> posts, int k) {
        if (posts == null || posts.isEmpty() || k <= 0) return new ArrayList<>();

        
        PriorityQueue<Post> minHeap = new PriorityQueue<>(k, (a, b) -> {
            int diff = a.getLikes() - b.getLikes();
            if (diff != 0) return diff;
            return Long.compare(a.getTimestamp(), b.getTimestamp()); 
        });

        for (Post p : posts) {
            if (minHeap.size() < k) {
                minHeap.offer(p);
            } else if (!minHeap.isEmpty()) {
                Post leastTrending = minHeap.peek();
                int  likeDiff      = p.getLikes() - leastTrending.getLikes();
                boolean moreRecent = p.getTimestamp() > leastTrending.getTimestamp();

                
                if (likeDiff > 0 || (likeDiff == 0 && moreRecent)) {
                    minHeap.poll();
                    minHeap.offer(p);
                }
            }
        }

        
        List<Post> result = new ArrayList<>(minHeap.size());
        while (!minHeap.isEmpty()) result.add(0, minHeap.poll()); 
        return result;
    }
}
