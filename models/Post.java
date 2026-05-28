package models;

import java.util.HashSet;
import java.util.Set;

public class Post {

    private final String postId;
    private final String authorUsername;
    private       String imageUrl;
    private       Set<String> likedBy;   
    private final long timestamp;

    public Post(String postId, String authorUsername, String imageUrl) {
        this.postId          = postId;
        this.authorUsername  = authorUsername;
        this.imageUrl        = imageUrl;
        this.likedBy         = new HashSet<>();
        this.timestamp       = System.currentTimeMillis();
    }

    
    public Post(String postId, String authorUsername, String imageUrl, long timestamp) {
        this.postId         = postId;
        this.authorUsername = authorUsername;
        this.imageUrl       = imageUrl;
        this.likedBy        = new HashSet<>();
        this.timestamp      = timestamp;
    }


    public String getPostId()          { return postId; }
    public String getAuthorUsername()  { return authorUsername; }
    public String getImageUrl()        { return imageUrl; }
    public long   getTimestamp()       { return timestamp; }
    public Set<String> getLikedBy()    { return likedBy; }

  
    public int getLikes() { return likedBy.size(); }

    public boolean isLikedBy(String username) { return likedBy.contains(username); }

   

    public void addLikedBy(String username)    { likedBy.add(username); }
    public void removeLikedBy(String username) { likedBy.remove(username); }

   
    public boolean toggleLike(String username) {
        if (likedBy.contains(username)) {
            likedBy.remove(username);
            return false;
        } else {
            likedBy.add(username);
            return true;
        }
    }
}
