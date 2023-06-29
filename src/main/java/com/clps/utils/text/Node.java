package com.clps.utils.text;

import java.util.List;

public
class Node {
    private String pid;
    private String id;
    private String text;
    private List<Node> children;

    public Node(String pid, String id, String text) {
        this.pid = pid;
        this.id = id;
        this.text = text;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}