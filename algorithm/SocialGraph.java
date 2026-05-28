package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class SocialGraph {

    private final Map<String, Set<String>> adj = new HashMap<>();

    public synchronized void addUser(String username) {
        adj.putIfAbsent(username, new HashSet<>());
    }

    public synchronized void removeUser(String username) {
        adj.remove(username);
        for (Set<String> s : adj.values()) s.remove(username);
    }

    public synchronized void follow(String from, String to) {
        adj.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public synchronized void unfollow(String from, String to) {
        Set<String> s = adj.get(from);
        if (s != null) s.remove(to);
    }

    public Set<String> getFollowing(String username) {
        return adj.getOrDefault(username, new HashSet<>());
    }

    
    public List<String> recommendUsers(String username) {
        Set<String> alreadyFollowing = getFollowing(username);
        Set<String> seen = new HashSet<>();
        seen.add(username);
        seen.addAll(alreadyFollowing);

        List<String> recommendations = new ArrayList<>();
        Queue<String> queue = new LinkedList<>(alreadyFollowing);

        while (!queue.isEmpty() && recommendations.size() < 10) {
            String current = queue.poll();
            for (String fof : getFollowing(current)) {
                if (!seen.contains(fof)) {
                    seen.add(fof);
                    recommendations.add(fof);
                }
            }
        }
        return recommendations;
    }
}
