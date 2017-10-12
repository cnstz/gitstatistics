package edu.df.git;

public class Tag {
    private final String tag;
    private Commit commit;

    Tag(String tag) {
        this.tag = tag;
        this.commit = null;
    }

    String getTag() {
        return tag;
    }

    void setCommit(Commit commit) {
        this.commit = commit;
    }

    @Override
    public String toString() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag1 = (Tag) o;

        return tag != null ? tag.equals(tag1.tag) : tag1.tag == null;
    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }
}
