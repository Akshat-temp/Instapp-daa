package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserTrie {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
        String  originalWord = null;  
    }

    private final TrieNode root = new TrieNode();

    
    public synchronized void insert(String username) {
        TrieNode cur = root;
        for (char c : username.toLowerCase().toCharArray()) {
            cur.children.putIfAbsent(c, new TrieNode());
            cur = cur.children.get(c);
        }
        cur.isEndOfWord    = true;
        cur.originalWord   = username;   
    }

    
    public List<String> searchByPrefix(String prefix) {
        TrieNode cur = root;
        String lp = prefix.toLowerCase();
        for (char c : lp.toCharArray()) {
            if (!cur.children.containsKey(c)) return new ArrayList<>();
            cur = cur.children.get(c);
        }
        List<String> results = new ArrayList<>();
        collectWords(cur, new StringBuilder(lp), results);
        return results.subList(0, Math.min(7, results.size()));
    }

    private void collectWords(TrieNode node, StringBuilder word, List<String> results) {
        if (results.size() >= 7) return; // early exit
        if (node.isEndOfWord) results.add(node.originalWord); // return original case
        for (Map.Entry<Character, TrieNode> e : node.children.entrySet()) {
            word.append(e.getKey());
            collectWords(e.getValue(), word, results);
            word.deleteCharAt(word.length() - 1);
        }
    }

    
    public synchronized void delete(String username) {
        deleteHelper(root, username.toLowerCase(), 0);
    }

    private boolean deleteHelper(TrieNode cur, String word, int idx) {
        if (idx == word.length()) {
            if (!cur.isEndOfWord) return false;
            cur.isEndOfWord = false;
            return cur.children.isEmpty();
        }
        char c = word.charAt(idx);
        TrieNode next = cur.children.get(c);
        if (next == null) return false;
        if (deleteHelper(next, word, idx + 1)) cur.children.remove(c);
        return !cur.isEndOfWord && cur.children.isEmpty();
    }
}
