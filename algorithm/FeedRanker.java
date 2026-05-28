package algorithm;

import models.Post;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  FeedRanker  —  Max-Heap based feed ranking
 * ═══════════════════════════════════════════════════════════════════
 *
 *  DATA STRUCTURE : Max-Heap (PriorityQueue)
 *  TIME COMPLEXITY: O(n log n)
 *
 *  Ranks posts by likes descending, then recency descending on tie.
 *
 *  Two modes used by FeedScreen:
 *
 *  1. getRankedFeed(posts)
 *     Ranks all given posts — used for the following-people feed
 *     and as a fallback for the full trending feed.
 *
 *  2. getFollowingFeed(allPosts, following)
 *     Filters to only posts whose author is in the following set,
 *     then ranks them. Returns only those posts.
 * ═══════════════════════════════════════════════════════════════════
 */
public class FeedRanker {

    private static final Comparator<Post> BY_LIKES_THEN_RECENCY = (a, b) -> {
        int diff = b.getLikes() - a.getLikes();
        if (diff != 0) return diff;
        return Long.compare(b.getTimestamp(), a.getTimestamp());
    };

    /** Rank all posts in the given list — most liked first. */
    public List<Post> getRankedFeed(List<Post> posts) {
        return drain(posts);
    }

    /**
     * Return only posts from followed users, ranked by likes desc.
     * Used as the first section of the feed for users who follow people.
     */
    public List<Post> getFollowingFeed(List<Post> allPosts, Set<String> following) {
        List<Post> filtered = new ArrayList<>();
        for (Post p : allPosts)
            if (following.contains(p.getAuthorUsername()))
                filtered.add(p);
        return drain(filtered);
    }

    /** Pour posts into a Max-Heap and drain in sorted order. */
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
