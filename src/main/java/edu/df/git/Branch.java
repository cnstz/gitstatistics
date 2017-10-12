package edu.df.git;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Branch {
    private final String name;
    private Date authorDate;
    private Date lastCommitDate;
    private final ArrayList<Commit> commits;
    private double commitPercentage;
    private final HashMap<Committer, Long> numberOfCommitsPerCommitter;
    private final HashMap<Committer, Double> percentageOfCommitsPerCommitter;

    Branch(String branchName) {
        this.name = branchName;
        this.commits = new ArrayList<>();
        this.authorDate = null;
        this.lastCommitDate = null;
        this.commitPercentage = 0.0;
        this.numberOfCommitsPerCommitter = new HashMap<>();
        this.percentageOfCommitsPerCommitter = new HashMap<>();
    }


    void calculatePercentages(long numberOfAllCommits) {
        commitPercentage = (commits.size() / (double) numberOfAllCommits) * 100;

        for (Commit commit : commits) {
            if (!numberOfCommitsPerCommitter.containsKey(commit.getCommitter())) {
                numberOfCommitsPerCommitter.put(commit.getCommitter(), 1L);
            } else {
                numberOfCommitsPerCommitter.put(commit.getCommitter(), numberOfCommitsPerCommitter.get(commit.getCommitter()) + 1);
            }
        }
        for (Map.Entry<Committer, Long> entry : numberOfCommitsPerCommitter.entrySet()) {
            percentageOfCommitsPerCommitter.put(entry.getKey(), (entry.getValue() / (double) commits.size() * 100));
        }
    }

    public Date getAuthorDate() {
        return authorDate;
    }

    void setAuthorDate(Date authorDate) {
        this.authorDate = authorDate;
    }

    public Date getLastCommitDate() {
        return lastCommitDate;
    }

    void setLastCommitDate(Date lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }

    public String getName() {
        return name;
    }

    public double getCommitPercentage() {
        return commitPercentage;
    }

    public HashMap<Committer, Double> getPercentageOfCommitsPerCommitter() {
        return percentageOfCommitsPerCommitter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branch branch = (Branch) o;

        return name != null ? name.equals(branch.name) : branch.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
