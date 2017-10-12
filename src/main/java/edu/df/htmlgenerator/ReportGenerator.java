package edu.df.htmlgenerator;

import edu.df.git.Branch;
import edu.df.git.Commit;
import edu.df.git.Committer;
import edu.df.git.Repository;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

import static j2html.TagCreator.*;

public class ReportGenerator {
    final private Repository repository;
    final private File outputDir;
    final private File htmlDir;

    public ReportGenerator(Repository repository, String outputPath) {
        this.repository = repository;
        String outputPath1 = outputPath;
        this.outputDir = new File(outputPath);
        if (!outputDir.exists())
            outputDir.mkdir();
        File cssDir = new File(outputPath + "/css");
        if (!cssDir.exists())
            cssDir.mkdir();
        File imgDir = new File(outputPath + "/img");
        if (!imgDir.exists())
            imgDir.mkdir();
        htmlDir = new File(outputPath + "/html");
        if (!htmlDir.exists())
            htmlDir.mkdir();
        File jsDir = new File(outputPath + "/js");
        if (!jsDir.exists())
            jsDir.mkdir();

    }

    public void generateHtmlReport() throws IOException {
        ContainerTag head = generateHtmlHead();
        ContainerTag header = generateHtmlHeader();
        ContainerTag footer = generateHtmlFooter();

        ContainerTag indexBody = generateHtmlIndexBody();
        ContainerTag branchesBody = generateHtmlBranchesBody();
        ContainerTag statisticsBody = generateHtmlStatisticsBody();

        String htmlIndex = generateHtmlOutput(head, header, indexBody, footer);
        String htmlBranches = generateHtmlOutput(head, header, branchesBody, footer);
        String htmlStatistics = generateHtmlOutput(head, header, statisticsBody, footer);

        HashMap<String, String> htmlStrings = new HashMap<>();
        htmlStrings.put("index.html", htmlIndex);
        htmlStrings.put("branches.html", htmlBranches);
        htmlStrings.put("statistics.html", htmlStatistics);

        generateFiles(htmlStrings);

    }

    private ContainerTag generateHtmlHead() {
        return head().with(meta().withCharset("UTF-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1"),
                meta().withName("description").withContent("Git repository analytics and statistics."),
                link().withRel("shortcut icon").withHref("../img/favicon.ico").withType("image/x-icon"),
                title("GIT Statistics - " + repository.getRepositoryName()),
                link().withRel("stylesheet").withHref("../css/milligram.min.css"),
                link().withRel("stylesheet").withHref("../css/fonts.css"),
                link().withHref("stylesheet").withHref("../css/normalize.css"),
                script().withSrc("../js/scripts.js"));
    }

    private ContainerTag generateHtmlHeader() {
        return header().
                withClass("row").withStyle(
                "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                        "max-width:1080px;" +
                        "margin: 0 auto !important;" +
                        "float: none !important;font-size:2.5em;").with(
                (div().withClass("column column-25").
                        with(
                                a().withHref("./index.html").with(
                                        img().withSrc("../img/logo.png").withAlt("GS logo")
                                ))),
                (div().
                        withClass("nav").
                        withClass("column column-55 column-offset-20").
                        withStyle("word-spacing: 30px;").
                        with(
                                a("Home").withHref("./index.html"),
                                a("Branches").withHref("./branches.html"),
                                a("Statistics").withHref("./statistics.html")
                        )));
    }

    private ContainerTag generateHtmlIndexBody() {
        return body().
                withClass("container").
                with(div().withClass("row").withStyle(
                        "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                "max-width:1080px;" +
                                "margin: 0 auto !important;" +
                                "float: none !important;").withClass("column column-100").
                                with(h1(repository.getRepositoryName()).withStyle("text-align:center;")),
                        div().withClass("row").withStyle(
                                "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                        "max-width:1080px;" +
                                        "margin: 0 auto !important;" +
                                        "float: none !important;font-size:1.5em;").withClass("column column-100").
                                with(
                                        table().
                                                with(
                                                        tbody().
                                                                with(
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Files"), td().withText(String.valueOf(repository.getNumberOfFiles()))),
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Lines"), td().withText(String.valueOf(repository.getNumberOfLines()))),
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Branches"), td().withText(String.valueOf(repository.getNumberOfBranches()))),
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Tags"), td().withText(String.valueOf(repository.getNumberOfTags()))),
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Committers"), td().withText(String.valueOf(repository.getNumberOfCommitters())))))));
    }

    private ContainerTag generateHtmlBranchesBody() {
        return body().
                withClass("container").
                with(div().withClass("row").withStyle(
                        "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                "max-width:1080px;" +
                                "margin: 0 auto !important;" +
                                "float: none !important;").withClass("column column-100").
                                with(h1(repository.getRepositoryName()).withStyle("text-align:center;")),
                        div().withClass("row").withStyle(
                                "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                        "max-width:1080px;" +
                                        "margin: 0 auto !important;" +
                                        "float: none !important;font-size:1.5em;").withClass("column column-100").
                                with(table().
                                        with(
                                                tbody().
                                                        with(
                                                                tr().with(td().with(h4("Branch")), td().with(h4("Creation Date")), td().with(h4("Last Commit"))),
                                                                tr().withClass("branch").with(createBranchesTable(repository.getBranches()))))),
                        div().withClass("column column-100").
                                with(createBranchesHiddenDivs(repository.getBranches())));

    }

    private List<DomContent> createBranchesTable(HashMap<String, Branch> branches) {
        List<DomContent> domContent = new ArrayList<>();
        for (Branch branch : branches.values()) {
            domContent.add(
                    tr().withClass("branches").with(td().withText(
                            branch.getName().substring(branch.getName().lastIndexOf("/") + 1)),
                            td().withText(branch.getAuthorDate().toString()),
                            td().withText(branch.getLastCommitDate().toString())).attr("onclick", "showCommits(\""
                            + (branch.getName().substring(branch.getName().lastIndexOf("/") + 1)) + "\")").withStyle("cursor:pointer;"));

        }
        return domContent;
    }

    private List<DomContent> createBranchesHiddenDivs(HashMap<String, Branch> branches) {
        List<DomContent> domContent = new ArrayList<>();
        for (Branch branch : branches.values()) {
            domContent.add(
                    div().withStyle("z-index:10;overflow-y:auto;visibility:hidden;display: inline-block;\n" +
                            "    position: fixed;\n" +
                            "    top: 0;\n" +
                            "    bottom: 0;\n" +
                            "    left: 0;\n" +
                            "    right: 0;\n" +
                            "    width: 95%;\n" +
                            "    height: 95%;\n" +
                            "    margin: auto;\n" +
                            "    background-color: #f3f3f3;" +
                            "padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;").
                            withClass("row").withClass("column column-50").withId(branch.getName().substring(branch.getName().lastIndexOf("/") + 1)).with(
                            h1().withId("h" + branch.getName().substring(branch.getName().lastIndexOf("/") + 1))).withClass("column column-50").
                            with(a("exit").withClass("button").attr("onclick", "hideCommits(\"" + branch.getName().substring(branch.getName().lastIndexOf("/") + 1) + "\")")).
                            withClass("row").withClass("column column-100").with(
                            table().with(tbody().
                                    with(tr().with(td().with(h4("Commit")), td().with(h4("Message")), td().with(h4("Date")), td().with(h4("Author")), td().with(h4("Tag"))),
                                            tr().withClass("commit").with(createCommitsTable(repository.getBranches().get(branch.getName()).getCommits()))))));
        }
        return domContent;
    }

    private List<DomContent> createCommitsTable(ArrayList<Commit> commits) {
        List<DomContent> domContent = new ArrayList<>();
        for (Commit commit : commits) {
            domContent.add(
                    tr().withClass("commits").withId(commit.getName()).with(td().withText(
                            commit.getName()), td().withText(commit.getFullMessage()), td().withText(commit.getDate().toString()),
                            td().withText(commit.getCommitter().getEmail()),
                            td().withText((commit.getTag() == null) ? "-" : commit.getTag().toString().substring(commit.getTag().toString().lastIndexOf("/") + 1))));

        }
        return domContent;
    }


    private ContainerTag generateHtmlStatisticsBody() {
        return body().
                withClass("container").
                with(div().withClass("row").withStyle(
                        "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                "max-width:1080px;" +
                                "margin: 0 auto !important;" +
                                "float: none !important;").withClass("column column-100").
                                with(h1(repository.getRepositoryName()).withStyle("text-align:center;")),
                        div().withClass("row").withStyle(
                                "background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;" +
                                        "max-width:1080px;" +
                                        "margin: 0 auto !important;" +
                                        "float: none !important;font-size:1.5em;").withClass("column column-100").
                                with(
                                        table().
                                                with(
                                                        tbody().
                                                                with(
                                                                        tr().
                                                                                with(
                                                                                        td().withText("Number Of Commits"), td().withText(String.valueOf(repository.getNumberOfAllCommits()))))),
                                        table().
                                                with(
                                                        tbody().
                                                                with(
                                                                        tr().with(td().with(h4("Name")), td().with(h4("Email")), td().with(h4("Commit Percentage"))),
                                                                        tr().withClass("committer").with(createCommitsPercentagePerCommitter(repository.getCommitters())
                                                                        ))),
                                        table().
                                                with(
                                                        tbody().
                                                                with(
                                                                        tr().with(td().with(h4("Branch")), td().with(h4("Commit Percentage"))),
                                                                        tr().with(createCommitsPercentagePerBranch(repository.getBranches())
                                                                        ))),
                                        div().withClass("column column-100").
                                                with(createStatisticsHiddenDivs(repository.getCommitters()))
                                ));
    }

    private List<DomContent> createCommitsPercentagePerCommitter(HashMap<String, Committer> committers) {
        List<DomContent> domContent = new ArrayList<>();
        for (Committer committer : committers.values()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().attr("onclick", "showCommitterInfo(\"" + committer.getEmail() + "\")").withClass("committers").with(td().withText(
                            committer.getName()), td().withText(committer.getEmail()), td().withText(df.format(committer.getCommitPercentage()) + "%")).
                            withStyle("cursor:pointer;"));

        }
        return domContent;
    }

    private Iterable<? extends DomContent> createCommitsPercentagePerBranch(HashMap<String, Branch> branches) {
        List<DomContent> domContent = new ArrayList<>();
        for (Branch branch : branches.values()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().with(td().withText(
                            branch.getName().substring(branch.getName().lastIndexOf("/") + 1)), td().withText(df.format(branch.getCommitPercentage()) + "%")));
        }
        return domContent;
    }

    private List<DomContent> createStatisticsHiddenDivs(HashMap<String, Committer> committers) {
        List<DomContent> domContent = new ArrayList<>();
        for (Committer committer : committers.values()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    div().withStyle("z-index:10;overflow-y:auto;visibility:hidden;display: inline-block;\n" +
                            "    position: fixed;\n" +
                            "    top: 0;\n" +
                            "    bottom: 0;\n" +
                            "    left: 0;\n" +
                            "    right: 0;\n" +
                            "    width: 95%;\n" +
                            "    height: 95%;\n" +
                            "    margin: auto;\n" +
                            "    background-color: #f3f3f3;" +
                            "padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:20px;").
                            withClass("row").withClass("column column-50").withId(committer.getEmail()).with(
                            h1().withId("h" + committer.getEmail())).withClass("column column-50").
                            with(a("exit").withClass("button").attr("onclick", "hideCommitterInfo(\"" + committer.getEmail() + "\")")).
                            withClass("row").withClass("column column-100").with(
                            table().with(tbody().
                                    with(tr().with(td().with(h4("Repository")), td().with(h4("Commit Percentage")))
                                            , tr().with(createCommitsPercentagePerCommitterPerBranch(committer))).
                                    withClass("row").withClass("column column-100").with(
                                    table().with(tbody().
                                            with(tr().with(td().with(h4("Lines Added Percentage")), td().with(h4("Lines Removed Percentage")), td().with(h4("Files Change"))),
                                                    tr().with(td().withText(df.format(committer.getLinesAddedPercentage()) + "%"), td().withText(df.format(committer.getLinesDeletedPercentage()) + "%"),
                                                            td().withText(df.format(committer.getFilesChangedPercentage()) + "%"))))).
                                    withClass("row").withClass("column column-100").with(
                                    table().with(tbody().
                                            with(tr().with(td().with(h4("Commit percentage per day"))),
                                                    tr().withClass("perday").with(createCommitPercentagePerDay(committer))))).
                                    withClass("row").withClass("column column-100").with(
                                    table().with(tbody().
                                            with(tr().with(td().with(h4("Commit percentage per week"))),
                                                    tr().withClass("perweek").with(createCommitPercentagePerWeek(committer)))).
                                            withClass("row").withClass("column column-100").with(
                                            table().with(tbody().
                                                    with(tr().with(td().with(h4("Commit percentage per month"))),
                                                            tr().withClass("permonth").with(createCommitPercentagePerMonth(committer)))))))));
        }
        return domContent;
    }

    private List<DomContent> createCommitsPercentagePerCommitterPerBranch(Committer committer) {
        List<DomContent> domContent = new ArrayList<>();
        for (Branch branch : repository.getBranches().values()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().with(
                            td().withText(branch.getName().substring(branch.getName().lastIndexOf("/") + 1)),
                            td().withText((branch.getPercentageOfCommitsPerCommitter().get(committer) == null) ? "0%" : df.format(branch.getPercentageOfCommitsPerCommitter().get(committer)) + "%")));
        }
        return domContent;
    }

    private List<DomContent> createCommitPercentagePerDay(Committer committer) {
        List<DomContent> domContent = new ArrayList<>();
        for (Map.Entry<Date, Double> e : committer.getCommitsPerDayPercentage().entrySet()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().with(
                            td().withText(e.getKey().toString().replaceAll("/*00:00:00 [a-zA-Z]*/*", "")),
                            td().withText((e.getValue() == null) ? "0%" : df.format(e.getValue()) + "%")));
        }
        return domContent;
    }

    private List<DomContent> createCommitPercentagePerWeek(Committer committer) {
        List<DomContent> domContent = new ArrayList<>();
        for (Map.Entry<String, Double> e : committer.getCommitsPerWeekPercentage().entrySet()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().with(
                            td().withText(e.getKey().substring(0, 1) + " week of " +
                                    Month.of(1 + Integer.parseInt(e.getKey().substring(2, e.getKey().lastIndexOf("_")))).getDisplayName(TextStyle.FULL, Locale.US) + " " +
                                    e.getKey().substring(e.getKey().lastIndexOf("_") + 1)),
                            td().withText((e.getValue() == null) ? "0%" : df.format(e.getValue()) + "%")));
        }
        return domContent;
    }

    private List<DomContent> createCommitPercentagePerMonth(Committer committer) {
        List<DomContent> domContent = new ArrayList<>();
        for (Map.Entry<String, Double> e : committer.getCommitsPerMonthPercentage().entrySet()) {
            DecimalFormat df = new DecimalFormat("#.##");
            domContent.add(
                    tr().with(
                            td().withText(Month.of(1 + Integer.parseInt(e.getKey().substring(0, e.getKey().indexOf("_")))).getDisplayName(TextStyle.FULL, Locale.US) + " " +
                                    e.getKey().substring(e.getKey().lastIndexOf("_") + 1)),
                            td().withText((e.getValue() == null) ? "0%" : df.format(e.getValue()) + "%")));
        }
        return domContent;
    }


    private ContainerTag generateHtmlFooter() {
        return footer().withStyle("").withClass("row").withStyle(
                "z-index:-9;background-color:rgba(255, 255, 255, 0.75); padding-left:20px; padding-right:20px;padding-top:20px; padding-bottom:200px;" +
                        "max-width:1080px;" +
                        "margin: 0 auto !important;" +
                        "float: none !important;font-size:1.5em;").with(
                (div().withClass("column column-100").with(h3("Konstantinos Dalianis - Maria Fava")).withStyle("text-align:center; position:absolute; bottom:0;")));
    }


    private String generateHtmlOutput(ContainerTag head, ContainerTag header, ContainerTag body, ContainerTag footer) {
        return document().render() + html().withStyle("background-image:url(\"../img/background.jpg\");").attr("lang", "eng").with(head, header, body, footer).render();
    }

    private void generateFiles(HashMap<String, String> htmlStrings) throws IOException {
        try {
            copyResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, String> html : htmlStrings.entrySet())
            writeFile(html.getKey(), html.getValue());
    }

    private void copyResources() throws IOException {
        File favicon = getResourceAsFile("favicon.ico");
        if (new File(outputDir.toPath() + "/img/favicon.ico").exists())
            Files.copy(favicon.toPath(), Paths.get(outputDir.toPath() + "/img/favicon.ico"));
        File logo = getResourceAsFile("logo.png");
        if (!new File(outputDir.toPath() + "/img/logo.png").exists())
            Files.copy(logo.toPath(), Paths.get(outputDir.toPath() + "/img/logo.png"));
        File background = getResourceAsFile("background.jpg");
        if (!new File(outputDir.toPath() + "/img/background.jpg").exists())
            Files.copy(background.toPath(), Paths.get(outputDir.toPath() + "/img/background.jpg"));
        File milligramCss = getResourceAsFile("milligram.min.css");
        if (!new File(outputDir.toPath() + "/css/milligram.min.css").exists())
            Files.copy(milligramCss.toPath(), Paths.get(outputDir.toPath() + "/css/milligram.min.css"));
        File normalizeCss = getResourceAsFile("normalize.css");
        if (!new File(outputDir.toPath() + "/css/normalize.css").exists())
            Files.copy(normalizeCss.toPath(), Paths.get(outputDir.toPath() + "/css/normalize.css"));
        File fontsCss = getResourceAsFile("fonts.css");
        if (!new File(outputDir.toPath() + "/css/fonts.css").exists())
            Files.copy(fontsCss.toPath(), Paths.get(outputDir.toPath() + "/css/fonts.css"));
        File scriptsJs = getResourceAsFile("scripts.js");
        if (!new File(outputDir.toPath() + "/js/scripts.js").exists())
            Files.copy(scriptsJs.toPath(), Paths.get(outputDir.toPath() + "/js/scripts.js"));
    }

    private File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeFile(String fileName, String html) throws IOException {
        File htmlFile = new File(htmlDir.getAbsolutePath() + "/" + fileName);
        FileWriter fstream = new FileWriter(htmlFile, false);
        BufferedWriter out = new BufferedWriter(fstream);
        html = html.replaceAll(">", ">\n");
        out.write(html);
        out.close();
    }
}
