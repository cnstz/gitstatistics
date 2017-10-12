package edu.df.files;

public class FileInfo {
    private final String extension;
    private int numberOfFiles;
    private int numberOfLines;

    public FileInfo(String extension) {
        this.extension = extension;
        this.numberOfFiles = 1;
        this.numberOfLines = 0;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return extension.equals(fileInfo.extension);
    }

    @Override
    public int hashCode() {
        return extension.hashCode();
    }

    @Override
    public String toString() {
        return "{\"FileInfo\":{"
                + "\"extension\":\"" + extension + "\""
                + ",\"numberOfFiles\":\"" + numberOfFiles + "\""
                + ",\"numberOfLines\":\"" + numberOfLines + "\""
                + "}}";
    }
}
