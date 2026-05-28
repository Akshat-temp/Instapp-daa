package models;

import algorithm.FeedRanker;
import algorithm.TrendingTracker;
import algorithm.SocialGraph;
import algorithm.UserTrie;
import models.Post;
import models.User;
import storage.Database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppState {

    
    public static final Map<String, User> userStore = new HashMap<>();
    public static final Map<String, Post> postStore = new HashMap<>();
    public static int postCounter = 1;

    
    public static final UserTrie        trie     = new UserTrie();
    public static final TrendingTracker trending = new TrendingTracker();
    public static final SocialGraph graph   = new SocialGraph();
    public static final FeedRanker  ranker  = new FeedRanker();


    public static User currentUser  = null;
    public static User viewingUser  = null;  

    
    public static void init() {
        Database.init();
        loadFromDatabase();
    }

    private static void loadFromDatabase() {
       
        List<User> users = Database.getAllUsers();
        for (User u : users) {
            userStore.put(u.getUsername(), u);
            trie.insert(u.getUsername());
            graph.addUser(u.getUsername());
        }

       
        List<Post> posts = Database.getAllPosts();
        for (Post p : posts) {
            postStore.put(p.getPostId(), p);
            User author = userStore.get(p.getAuthorUsername());
            if (author != null) author.addPostId(p.getPostId());

            
            try {
                int n = Integer.parseInt(p.getPostId().substring(1));
                if (n >= postCounter) postCounter = n + 1;
            } catch (NumberFormatException ignored) {}
        }

        
        for (String[] f : Database.getAllFollows()) {
            String from = f[0], to = f[1];
            if (userStore.containsKey(from) && userStore.containsKey(to)) {
                graph.follow(from, to);
                userStore.get(from).addFollowing(to);
                userStore.get(to).addFollower(from);
            }
        }

       
        for (String[] l : Database.getAllLikes()) {
            String username = l[0], postId = l[1];
            Post p = postStore.get(postId);
            if (p != null) {
                p.addLikedBy(username);
                User u = userStore.get(username);
                if (u != null) u.addLikedPost(postId);
            }
        }

        System.out.printf("[State] Loaded %d users, %d posts from database.%n",
                userStore.size(), postStore.size());
    }
}
