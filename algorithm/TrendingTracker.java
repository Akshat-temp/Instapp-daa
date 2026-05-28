package algorithm;

import models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  TrendingTracker  —  Top-K Trending Posts using a Min-Heap
 * ═══════════════════════════════════════════════════════════════════
 *
 *  DATA STRUCTURE : Min-Heap (PriorityQueue) of fixed size K
 *  TIME COMPLEXITY: O(n log k)  where n = total posts, k = result size
 *  SPACE          : O(k)        only K posts held in memory at once
 *
 *  WHY MIN-HEAP FOR TOP-K? (not Max-Heap like FeedRanker)
 *  ─────────────────────────────────────────────────────────────────
 *  FeedRanker ranks ALL posts — it dumps everything into a Max-Heap
 *  and drains it. That is O(n log n) in both time and space.
 *
 *  TrendingTracker only wants the TOP K posts out of n total.
 *  Trick: keep a Min-Heap of exactly size K.
 *    • If a new post has MORE likes than the heap's minimum → replace it.
 *    • If it has FEWER likes → skip it.
 *  After scanning all n posts, the heap holds exactly the K most liked.
 *  Then drain the heap in reverse → descending order.
 *
 *  Diagram (K = 3, likes shown):
 *
 *    Posts: [2, 8, 1, 5, 9, 3, 7]
 *
 *    Process 2  → heap: [2]
 *    Process 8  → heap: [2, 8]
 *    Process 1  → heap: [1, 2, 8]       ← full now
 *    Process 5  → 5 > min(1) → replace  → heap: [2, 5, 8]
 *    Process 9  → 9 > min(2) → replace  → heap: [5, 8, 9]
 *    Process 3  → 3 < min(5) → skip
 *    Process 7  → 7 > min(5) → replace  → heap: [7, 8, 9]
 *
 *    Drain reversed → [9, 8, 7]  ✓ top 3
 *
 *  INSTAGRAM USE CASE
 *  ─────────────────────────────────────────────────────────────────
 *  New users who follow nobody see trending posts.
 *  Existing users see following-posts first, then trending posts
 *  from the rest of the platform fill the remainder of their feed.
 *
 * ═══════════════════════════════════════════════════════════════════
 */
public class TrendingTracker {

    /**
     * Returns the top K trending posts from the given list,
     * ordered by likes descending (then recency on tie).
     *
     * @param posts all candidate posts
     * @param k     number of trending posts to return
     * @return up to K posts, most liked first
     */
    public List<Post> getTopK(List<Post> posts, int k) {
        if (posts == null || posts.isEmpty() || k <= 0) return new ArrayList<>();

        // Min-Heap ordered by likes ASC (so min is always at top for easy eviction)
        PriorityQueue<Post> minHeap = new PriorityQueue<>(k, (a, b) -> {
            int diff = a.getLikes() - b.getLikes();
            if (diff != 0) return diff;
            return Long.compare(a.getTimestamp(), b.getTimestamp()); // older first on tie
        });

        for (Post p : posts) {
            if (minHeap.size() < k) {
                minHeap.offer(p);
            } else if (!minHeap.isEmpty()) {
                Post leastTrending = minHeap.peek();
                int  likeDiff      = p.getLikes() - leastTrending.getLikes();
                boolean moreRecent = p.getTimestamp() > leastTrending.getTimestamp();

                // Replace if this post is more liked, or equally liked but newer
                if (likeDiff > 0 || (likeDiff == 0 && moreRecent)) {
                    minHeap.poll();
                    minHeap.offer(p);
                }
            }
        }

        // Drain heap — comes out ascending, so reverse for descending
        List<Post> result = new ArrayList<>(minHeap.size());
        while (!minHeap.isEmpty()) result.add(0, minHeap.poll()); // prepend = reverse
        return result;
    }
}
