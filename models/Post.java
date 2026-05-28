package models;

import java.util.HashSet;
import java.util.Set;

public class Post {

    private final String     postId;
    private final String     authorUsername;
    private       String     imageUrl;    // original file path (kept for reference)
    private       String     imageData;  // Base64 encoded image — stored in DB
    private       Set<String> likedBy;
    private final long       timestamp;

    public Post(String postId, String authorUsername, String imageUrl) {
        this.postId         = postId;
        this.authorUsername = authorUsername;
        this.imageUrl       = imageUrl;
        this.imageData      = null;
        this.likedBy        = new HashSet<>();
        this.timestamp      = System.currentTimeMillis();
    }

    public Post(String postId, String authorUsername, String imageUrl, long timestamp) {
        this.postId         = postId;
        this.authorUsername = authorUsername;
        this.imageUrl       = imageUrl;
        this.imageData      = null;
        this.likedBy        = new HashSet<>();
        this.timestamp      = timestamp;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String      getPostId()         { return postId; }
    public String      getAuthorUsername() { return authorUsername; }
    public String      getImageUrl()       { return imageUrl; }
    public String      getImageData()      { return imageData; }
    public Set<String> getLikedBy()        { return likedBy; }
    public long        getTimestamp()      { return timestamp; }
    public int         getLikes()          { return likedBy.size(); }

    public boolean hasImageData() {
        return imageData != null && !imageData.isBlank();
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setImageData(String data) { this.imageData = data; }

    // ── Like helpers ──────────────────────────────────────────────────────────
    public void    addLikedBy(String u)    { likedBy.add(u); }
    public void    removeLikedBy(String u) { likedBy.remove(u); }
    public boolean isLikedBy(String u)     { return likedBy.contains(u); }

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