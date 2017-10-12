package edu.df.git;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

public class Committer {
    private final String name;
    private final String email;
    private long numberOfCommits, linesAdded, linesDeleted, filesChanged;
    private double commitPercentage, linesAddedPercentage, linesDeletedPercentage, filesChangedPercentage;
    private final TreeMap<Date, Long> commitsPerDay;
    private final TreeMap<Date, Double> commitsPerDayPercentage;
    private final TreeMap<String, Long> commitsPerWeek;
    private final TreeMap<String, Long> commitsPerMonth;
    private final TreeMap<String, Double> commitsPerWeekPercentage;
    private final TreeMap<String, Double> commitsPerMonthPercentage;

    Committer(String name, String email) {
        this.name = name;
        this.email = email;
        this.numberOfCommits = 0;
        this.linesAdded = 0;
        this.linesDeleted = 0;
        this.filesChanged = 0;
        this.commitPercentage = 0.0;
        this.linesAddedPercentage = 0.0;
        this.linesDeletedPercentage = 0.0;
        this.filesChangedPercentage = 0.0;
        this.commitsPerDay = new TreeMap<>();
        this.commitsPerDayPercentage = new TreeMap<>();
        this.commitsPerWeek = new TreeMap<>();
        this.commitsPerMonth = new TreeMap<>();
        this.commitsPerWeekPercentage = new TreeMap<>();
        this.commitsPerMonthPercentage = new TreeMap<>();
    }

    void calculatePercentages(long numberOfAllCommits, TreeMap<Date, Long> commitsPerDayForAll, TreeMap<String, Long> commitsPerWeekForAll, TreeMap<String, Long> commitsPerMonthForAll, long totalLinesAdded, long totalLinesDeleted, long totalFilesChanged) {
        this.commitPercentage = (numberOfCommits / (double) numberOfAllCommits) * 100;

        for (Date date : commitsPerDayForAll.keySet()) {
            if (this.commitsPerDay.containsKey(date)) {
                this.commitsPerDayPercentage.put(date, (this.commitsPerDay.get(date) / (double) commitsPerDayForAll.get(date) * 100));

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int commitWeek = cal.get(Calendar.WEEK_OF_MONTH) == 5 ? 4 : cal.get(Calendar.WEEK_OF_MONTH);
                int commitMonth = cal.get(Calendar.MONTH);
                int commitYear = cal.get(Calendar.YEAR);

                if (!commitsPerWeek.containsKey(commitWeek + "_" + commitMonth + "_" + commitYear)) {
                    commitsPerWeek.put(commitWeek + "_" + commitMonth + "_" + commitYear, this.commitsPerDay.get(date));
                } else {
                    commitsPerWeek.put(commitWeek + "_" + commitMonth + "_" + commitYear, commitsPerWeek.get(commitWeek + "_" + commitMonth + "_" + commitYear) + this.commitsPerDay.get(date));
                }

                if (!commitsPerMonth.containsKey(commitMonth + "_" + commitYear)) {
                    commitsPerMonth.put(commitMonth + "_" + commitYear, this.commitsPerDay.get(date));
                } else {
                    commitsPerMonth.put(commitMonth + "_" + commitYear, commitsPerMonth.get(commitMonth + "_" + commitYear) + this.commitsPerDay.get(date));
                }
            }
        }

        for (String week : this.commitsPerWeek.keySet())
            if (this.commitsPerWeek.get(week) != null && commitsPerWeekForAll.get(week) != null && commitsPerWeekForAll.get(week) != 0)
                this.commitsPerWeekPercentage.put(week, (this.commitsPerWeek.get(week) / (double) commitsPerWeekForAll.get(week) * 100));
            else
                this.commitsPerWeekPercentage.put(week, (double) 0);


        for (String month : this.commitsPerMonth.keySet())
            if (this.commitsPerMonth.get(month) != null && commitsPerMonthForAll.get(month) != null && commitsPerMonthForAll.get(month) != 0)
                this.commitsPerMonthPercentage.put(month, (this.commitsPerMonth.get(month) / (double) commitsPerMonthForAll.get(month) * 100));
            else
                this.commitsPerMonthPercentage.put(month, (double) 0);

        if (totalLinesAdded != 0)
            this.linesAddedPercentage = (linesAdded / (double) totalLinesAdded) * 100;
        if (totalLinesDeleted != 0)
            this.linesDeletedPercentage = (linesDeleted / (double) totalLinesDeleted) * 100;
        if (totalFilesChanged != 0)
            this.filesChangedPercentage = (filesChanged / (double) totalFilesChanged) * 100;


    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public double getCommitPercentage() {
        return commitPercentage;
    }

    public TreeMap<Date, Long> getCommitsPerDay() {
        return commitsPerDay;
    }

    public TreeMap<Date, Double> getCommitsPerDayPercentage() {
        return commitsPerDayPercentage;
    }

    public TreeMap<String, Long> getCommitsPerWeek() {
        return commitsPerWeek;
    }

    public TreeMap<String, Long> getCommitsPerMonth() {
        return commitsPerMonth;
    }

    public TreeMap<String, Double> getCommitsPerWeekPercentage() {
        return commitsPerWeekPercentage;
    }

    public TreeMap<String, Double> getCommitsPerMonthPercentage() {
        return commitsPerMonthPercentage;
    }

    public long getNumberOfCommits() {
        return numberOfCommits;
    }

    public void setNumberOfCommits(long numberOfCommits) {
        this.numberOfCommits = numberOfCommits;
    }

    public long getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(long linesAdded) {
        this.linesAdded = linesAdded;
    }

    public long getLinesDeleted() {
        return linesDeleted;
    }

    public void setLinesDeleted(long linesDeleted) {
        this.linesDeleted = linesDeleted;
    }

    public long getFilesChanged() {
        return filesChanged;
    }

    public void setFilesChanged(long filesChanged) {
        this.filesChanged = filesChanged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Committer committer = (Committer) o;

        return (name != null ? name.equals(committer.name) : committer.name == null) && (email != null ? email.equals(committer.email) : committer.email == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Committer{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", commitPercentage=" + commitPercentage +
                ", linesAddedPercentage=" + linesAddedPercentage +
                ", linesDeletedPercentage=" + linesDeletedPercentage +
                ", filesChangedPercentage=" + filesChangedPercentage +
                "}\n";
    }

    public double getLinesAddedPercentage() {
        return linesAddedPercentage;
    }

    public double getLinesDeletedPercentage() {
        return linesDeletedPercentage;
    }

    public double getFilesChangedPercentage() {
        return filesChangedPercentage;
    }
}
