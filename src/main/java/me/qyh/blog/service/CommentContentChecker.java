package me.qyh.blog.service;

@FunctionalInterface
public interface CommentContentChecker {

    void check(String content);

}
