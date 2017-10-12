package edu.df.git;

import java.util.Date;

public class Commit {
    private final String name;
    private final Date date;
    private final Committer committer;
    private final String fullMessage;
    private final Tag tag;

    Commit(String name, Committer committer, int commitTime, String fullMessage, Tag tag) {
        this.name = name;
        this.date = new Date(commitTime * 1000L);
        this.committer = committer;
        this.fullMessage = fullMessage;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public Committer getCommitter() {
        return committer;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        return name != null ? name.equals(commit.name) : commit.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{\"Commit\":{"
                + "\"name\":\"" + name + "\""
                + ",\"date\":" + date
                + ",\"committer\":" + committer
                + ",\"fullMessage\":\"" + fullMessage + "\""
                + "}}";
    }


}
