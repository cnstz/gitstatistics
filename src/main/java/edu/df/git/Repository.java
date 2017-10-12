package edu.df.git;

import edu.df.files.FileInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.util.*;

public class Repository {
    /**
     * Variables declaration.
     */
    private final String gitRepositoryPath;
    private final String repositoryName;
    private final Git git;

    private HashMap<String, FileInfo> filesInfo;
    private HashMap<String, Branch> branches;
    private HashMap<String, Commit> commits;
    private TreeMap<Date, Long> commitsPerDay;
    private TreeMap<String, Long> commitsPerWeek, commitsPerMonth;
    private HashMap<String, Tag> tags;
    private HashMap<String, Committer> committers;


    private long numberOfFiles, numberOfLines, numberOfBranches, numberOfTags, numberOfCommitters, numberOfAllCommits, totalFilesChanged, totalLinesAdded, totalLinesDeleted;

    /**
     * Repository constructor
     *
     * @param gitRepositoryPath
     * @throws IOException
     */
    public Repository(String gitRepositoryPath) throws IOException {
        this.gitRepositoryPath = gitRepositoryPath;
        String gitFolder = gitRepositoryPath + "/.git";
        this.git = Git.open(new File(gitFolder));
        this.repositoryName = new File(gitRepositoryPath).getName();


        this.filesInfo = new HashMap<>();
        this.branches = new HashMap<>();
        this.commits = new HashMap<>();
        this.commitsPerDay = new TreeMap<>();
        this.commitsPerWeek = new TreeMap<>();
        this.commitsPerMonth = new TreeMap<>();
        this.tags = new HashMap<>();
        this.committers = new HashMap<>();

        this.numberOfFiles = 0;
        this.numberOfFiles = 0;
        this.numberOfBranches = 0;
        this.numberOfTags = 0;
        this.numberOfCommitters = 0;

        this.totalFilesChanged = 0;
        this.totalLinesAdded = 0;
        this.totalLinesDeleted = 0;
    }

    /**
     * Analyzes a git repository.
     */
    public void analyze() {
        try {
            fileAnalysis();
        } catch (IOException e) {
            System.err.println("There was an error analyzing repository files.");
            e.printStackTrace();
        }
        System.out.println("Done fileAnalysis");
        try {
            repositoryAnalysis();
        } catch (GitAPIException | IOException e) {
            System.err.println("There was an error analyzing repository branches.");
            e.printStackTrace();
        }
        System.out.println("Done repositoryAnalysis");

        committersAnalysis();
        System.out.println("Done committersAnalysis");

        try {
            calculateLineChanges();
        } catch (IOException e) {
            System.err.println("There was an error calculating line changes.");
            e.printStackTrace();
        }
        System.out.println("Done calculateLineChanges");

        calculateGenericStatistics();
        System.out.println("Done calculateGenericStatistics");

        git.close();
    }

    /**
     * Calculates percentages.
     */
    private void calculateGenericStatistics() {
        for (FileInfo fileInfo : filesInfo.values()) {
            numberOfFiles += fileInfo.getNumberOfFiles();
            numberOfLines += fileInfo.getNumberOfLines();
        }
        numberOfBranches = branches.size();
        numberOfCommitters = committers.size();
        numberOfAllCommits = commits.size();
        numberOfTags = tags.size();

        /* Committers percentages */
        for (Committer committer : committers.values()) {
            committer.calculatePercentages(numberOfAllCommits, commitsPerDay, commitsPerWeek, commitsPerMonth, totalLinesAdded, totalLinesDeleted, totalFilesChanged);
        }

        /* Branches percentages */
        long allCommitsInAllBranches = 0;
        for (Branch branch : branches.values()) {
            allCommitsInAllBranches += branch.getCommits().size();
        }
        for (Branch branch : branches.values()) {
            branch.calculatePercentages(allCommitsInAllBranches);
        }
    }

    /**
     * Saves information for each file type found in the repository in HashMap<String, FileInfo> filesInfo
     * Key is the file extension, files with no extension are classified as "other"
     * <p>
     * Uses a stack to store all files - including folders - found and pop's them until the stack is empty
     *
     * @throws IOException
     */
    private void fileAnalysis() throws IOException {
        /* Analyze files */
        File gitRepositoryDirectory = new File(gitRepositoryPath);

        Stack<File> stack = new Stack<>();
        stack.push(gitRepositoryDirectory);

        while (!stack.isEmpty()) {
            File child = stack.pop();
            if (child.isDirectory() && !child.getName().equals(".git")) {
                // file is a directory -ignores .git directory-
                //noinspection ConstantConditions
                for (File f : child.listFiles())
                    stack.push(f);
            } else if (child.isFile()) {
                // file is a file
                if (child.getName().contains(".")) {
                    // file with extension
                    String fileExtension = child.getName().substring(child.getName().lastIndexOf("."));
                    addFile(fileExtension, child);
                } else {
                    // file with no extension
                    addFile("other", child);
                }
            }
        }
    }

    /**
     * Adds a file extension in HashMap<String, FileInfo> filesInfo if it doesn't exist
     * <p>
     * Increases number of files for that type of extension
     * Counts file's lines and increases number of lines for that type of extension
     *
     * @param fileExtension is the extension of the file
     */
    private void addFile(String fileExtension, File file) throws IOException {
        if (filesInfo.containsKey(fileExtension))
            filesInfo.get(fileExtension).setNumberOfFiles(filesInfo.get(fileExtension).getNumberOfFiles() + 1);
        else
            filesInfo.put(fileExtension, new FileInfo(fileExtension));
        filesInfo.get(fileExtension).setNumberOfLines(filesInfo.get(fileExtension).getNumberOfLines() + countFileLines(file));
    }

    /**
     * Analyses a repository and finds Branches, Commits, Committers, Tags
     *
     * @throws GitAPIException
     * @throws IOException
     */
    private void repositoryAnalysis() throws GitAPIException, IOException {
        List<Ref> refBranches = git.branchList().call();

        Iterable<RevCommit> commitsIterator = git.log().all().call();
        for (RevCommit commit : commitsIterator) {
            addCommit(commit);
            for (Ref branch : refBranches) {
                String branchName = branch.getName();
                addBranch(branchName);
                if (commitInBranch(commit, branchName))
                    addCommitToBranch(commit, branchName);
            }
        }
    }

    /**
     * Adds given commit to given branch
     *
     * @param commit
     * @param branchName
     */
    private void addCommitToBranch(RevCommit commit, String branchName) {
        if (!branches.get(branchName).getCommits().contains(commit.getName()))
            branches.get(branchName).getCommits().add(commits.get(commit.getName()));

        /* Check branch author date and change it, if needed */
        if (branches.get(branchName).getAuthorDate() == null
                || branches.get(branchName).getAuthorDate().after(commit.getAuthorIdent().getWhen()))
            branches.get(branchName).setAuthorDate(commit.getAuthorIdent().getWhen());

        /* Check branch last commit date and change it, if needed */
        if (branches.get(branchName).getLastCommitDate() == null
                || branches.get(branchName).getLastCommitDate().before(new Date(commit.getCommitTime() * 1000L)))
            branches.get(branchName).setLastCommitDate(new Date(commit.getCommitTime() * 1000L));
    }

    /**
     * Checks if given commit is in given branch
     *
     * @param commit
     * @param branchName
     * @return
     * @throws IOException
     */
    private boolean commitInBranch(RevCommit commit, String branchName) throws IOException {
        org.eclipse.jgit.lib.Repository repository = git.getRepository();
        RevWalk walk = new RevWalk(repository);
        RevCommit targetCommit = walk.parseCommit(repository.resolve(
                commit.getName()));

        for (Map.Entry<String, Ref> e : repository.getAllRefs().entrySet()) {
            if (e.getKey().startsWith(Constants.R_HEADS)) {
                if (walk.isMergedInto(targetCommit, walk.parseCommit(
                        e.getValue().getObjectId()))) {
                    String foundInBranch = e.getValue().getName();
                    if (branchName.equals(foundInBranch)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds a branch in HashMap<String, Branch> branches if it doesn't exist
     *
     * @param branchName is the name of the branch
     */
    private void addBranch(String branchName) {
        if (!branches.containsKey(branchName))
            branches.put(branchName, new Branch(branchName));
    }


    /**
     * Adds a commit in HashMap<String, Commit> commits if it doesn't exist
     *
     * @param commit to be added
     * @throws MissingObjectException
     * @throws GitAPIException
     */
    private void addCommit(RevCommit commit) throws IOException {
        String commitName = commit.getName();

        if (!commits.containsKey(commitName)) {
            // check if the commit has a tag
            Tag tag = getTag(commit);

            addCommitter(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress());

            Committer committer = committers.get(commit.getAuthorIdent().getEmailAddress());

            commits.put(commitName, new Commit(commit.getName(), committer, commit.getCommitTime(), commit.getFullMessage(), tag));

            if (tag != null)
                tag.setCommit(commits.get(commitName));

            updateCommitsStatistics(commitName);
            updateCommitterDailyCommits(committer, commitName);

        }
    }

    /**
     * Calculate commit statistics by date
     *
     * @param commitName
     */
    private void updateCommitsStatistics(String commitName) {
        Date commitDateWithoutTime = calculateDateWithoutTime(commits.get(commitName).getDate().getTime());
        if (!this.commitsPerDay.containsKey(commitDateWithoutTime)) {
            this.commitsPerDay.put(commitDateWithoutTime, 1L);
        } else {
            this.commitsPerDay.put(commitDateWithoutTime, this.commitsPerDay.get(commitDateWithoutTime) + 1);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(commitDateWithoutTime);
        int commitWeek = cal.get(Calendar.WEEK_OF_MONTH);
        int commitMonth = cal.get(Calendar.MONTH);
        int commitYear = cal.get(Calendar.YEAR);

        if (!commitsPerWeek.containsKey(commitWeek + "_" + commitMonth + "_" + commitYear)) {
            commitsPerWeek.put(commitWeek + "_" + commitMonth + "_" + commitYear, this.commitsPerDay.get(commitDateWithoutTime));
        } else {
            commitsPerWeek.put(commitWeek + "_" + commitMonth + "_" + commitYear, commitsPerWeek.get(commitWeek + "_" + commitMonth + "_" + commitYear) + 1);
        }

        if (!commitsPerMonth.containsKey(commitMonth + "_" + commitYear)) {
            commitsPerMonth.put(commitMonth + "_" + commitYear, this.commitsPerDay.get(commitDateWithoutTime));
        } else {
            commitsPerMonth.put(commitMonth + "_" + commitYear, commitsPerMonth.get(commitMonth + "_" + commitYear) + 1);
        }
    }

    /**
     * Updates a committer's daily commits
     *
     * @param committer
     * @param commitName
     */
    private void updateCommitterDailyCommits(Committer committer, String commitName) {
        Commit commit = commits.get(commitName);
        TreeMap<Date, Long> dailyCommits = committer.getCommitsPerDay();

        Date commitDateWithoutTime = calculateDateWithoutTime(commit.getDate().getTime());

        if (!dailyCommits.containsKey(commitDateWithoutTime)) {
            // Daily Commits of this author does not contain that date
            dailyCommits.put(commitDateWithoutTime, 1L);
        } else {
            // Daily Commits contain that date
            dailyCommits.put(commitDateWithoutTime, dailyCommits.get(commitDateWithoutTime) + 1);
        }
    }

    /**
     * Sets given date's time to 00:00:00
     *
     * @param time
     * @return
     */
    private Date calculateDateWithoutTime(long time) {
        Date date = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Generates statistics for each committer.
     */
    private void committersAnalysis() {
        for (Commit commit : commits.values()) {
            commit.getCommitter().setNumberOfCommits(commit.getCommitter().getNumberOfCommits() + 1);
        }
    }

    /**
     * Adds a committer in HashMap<String, Committer> committers if he doesn't exists
     *
     * @param name         of the committer
     * @param emailAddress of the committer
     */
    private void addCommitter(String name, String emailAddress) {
        if (!committers.containsKey(emailAddress))
            committers.put(emailAddress, new Committer(name, emailAddress));
    }

    /**
     * Returns tag for a commit, if it exists
     *
     * @param commit
     * @return
     */
    private Tag getTag(RevCommit commit) throws IOException {
        Tag tag = null;

        RevWalk walk = new RevWalk(git.getRepository());

        for (Ref ref : git.getRepository().getTags().values()) {
            RevObject obj = walk.parseAny(ref.getObjectId());
            RevCommit tagCommit;
            if (obj instanceof RevCommit)
                tagCommit = (RevCommit) obj;
            else if (obj instanceof RevTag) {
                try {
                    tagCommit = walk.parseCommit(((RevTag) obj).getObject());
                } catch (Exception e) {
                    return tag;
                }
            } else
                continue;

            if (commit.equals(tagCommit)
                    || walk.isMergedInto(commit, tagCommit)) {
                tag = new Tag(ref.getName());
                tags.put(ref.getName(), tag);
            }
        }

        return tag;
    }


    /**
     * Calculates file changes and lines additions and deletions
     *
     * @throws IOException
     */
    private void calculateLineChanges() throws IOException {
        ArrayList<Commit> sortedByDateCommits = new ArrayList(commits.values());
        sortedByDateCommits.sort((Comparator.comparing(Commit::getDate)));

        org.eclipse.jgit.lib.Repository repository = git.getRepository();
        RevWalk rw = new RevWalk(repository);

        Commit commit1 = sortedByDateCommits.get(0);

        int linesAdded0 = 0;
        int filesChanged = 0, linesDeleted = 0, linesAdded = 0;

        for (int i = 1; i < sortedByDateCommits.size(); i++) {
            Commit commit2 = sortedByDateCommits.get(i);
            RevCommit revCommit1 = rw.parseCommit(repository.resolve(commit1.getName()));
            RevCommit revCommit2 = rw.parseCommit(repository.resolve(commit2.getName()));
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs;
            diffs = df.scan(revCommit1.getTree(), revCommit2.getTree());
            filesChanged += diffs.size();
            for (DiffEntry diff : diffs) {
                for (Edit edit : df.toFileHeader(diff).toEditList()) {
                    linesAdded += edit.getEndB() - edit.getBeginB();
                    linesDeleted += edit.getEndA() - edit.getBeginA();
                    if (i == 1) {
                        linesAdded0 += edit.getEndA();
                    }
                }
            }

            if (i == 1) {
                commit1.getCommitter().setLinesAdded(commit1.getCommitter().getLinesAdded() + linesAdded0);
            }

            totalFilesChanged += filesChanged;
            totalLinesAdded += linesAdded;
            totalLinesDeleted += linesDeleted;

            commit2.getCommitter().setLinesAdded(commit2.getCommitter().getLinesAdded() + linesAdded);
            commit2.getCommitter().setLinesDeleted(commit2.getCommitter().getLinesDeleted() + linesDeleted);
            commit2.getCommitter().setFilesChanged(commit2.getCommitter().getFilesChanged() + filesChanged);

            commit1 = sortedByDateCommits.get(i);
            linesAdded = 0;
            linesDeleted = 0;
            filesChanged = 0;
        }

    }

    /**
     * Counts number of lines in a file
     *
     * @param file
     * @return number of lines
     * @throws IOException
     */
    private int countFileLines(File file) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean endsWithoutNewLine = false;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n')
                        ++count;
                }
                endsWithoutNewLine = (c[readChars - 1] != '\n');
            }
            if (endsWithoutNewLine) {
                ++count;
            }
            return count;
        }
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public HashMap<String, Committer> getCommitters() {
        return committers;
    }

    public long getNumberOfFiles() {
        return numberOfFiles;
    }

    public long getNumberOfLines() {
        return numberOfLines;
    }

    public long getNumberOfBranches() {
        return numberOfBranches;
    }

    public long getNumberOfTags() {
        return numberOfTags;
    }

    public long getNumberOfCommitters() {
        return numberOfCommitters;
    }

    public long getNumberOfAllCommits() {
        return numberOfAllCommits;
    }

    public HashMap<String, Branch> getBranches() {
        return branches;
    }

    public HashMap<String, Commit> getCommits() {
        return commits;
    }

    public HashMap<String, Committer> getAuthors() {
        return committers;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "numberOfFiles=" + numberOfFiles +
                ", numberOfLines=" + numberOfLines +
                '}';
    }

}
