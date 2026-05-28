package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User {

    private String username;
    private String password;
    private List<String> followers;
    private List<String> following;
    private List<String> postIds;
    private Set<String> likedPosts; 
    private String      dpPath;     

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.followers  = new ArrayList<>();
        this.following  = new ArrayList<>();
        this.postIds    = new ArrayList<>();
        this.likedPosts = new HashSet<>();
        this.dpPath     = null;
    }


    public String getUsername()  { return username; }
    public String getPassword()  { return password; }

    public List<String> getFollowers()  { return followers; }
    public List<String> getFollowing()  { return following; }
    public List<String> getPostIds()    { return postIds; }
    public Set<String>  getLikedPosts() { return likedPosts; }

    public int getFollowerCount()  { return followers.size(); }
    public int getFollowingCount() { return following.size(); }
    public int getPostCount()      { return postIds.size(); }


    public void setUsername(String u) { this.username = u; }
    public void setPassword(String p) { this.password = p; }

    

    public void addFollower(String u)    { if (!followers.contains(u))  followers.add(u); }
    public void addFollowing(String u)   { if (!following.contains(u))  following.add(u); }
    public void removeFollower(String u) { followers.remove(u); }
    public void removeFollowing(String u){ following.remove(u); }

    public void addPostId(String id)     { if (!postIds.contains(id)) postIds.add(id); }

    public void addLikedPost(String postId)    { likedPosts.add(postId); }
    public void removeLikedPost(String postId) { likedPosts.remove(postId); }
    public boolean hasLiked(String postId)     { return likedPosts.contains(postId); }

    public String  getDpPath()           { return dpPath; }
    public void    setDpPath(String path){ this.dpPath = path; }
    public boolean hasDp()               { return dpPath != null && !dpPath.isBlank(); }
}
