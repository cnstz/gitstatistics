package edu.df.gitstatistics;

import edu.df.git.Repository;
import edu.df.htmlgenerator.ReportGenerator;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GitStatistics {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong number of arguments.\nPlease insert local git repository path and HTML output path.");
            return;
        }
        final String gitRepositoryPath = args[0].replaceAll("\\\\", "/");
        final String outputPath = args[1].replaceAll("\\\\", "/");

        try {
            final Repository repository = new Repository(gitRepositoryPath);
            repository.analyze();
            final ReportGenerator reportGenerator = new ReportGenerator(repository, outputPath);
            reportGenerator.generateHtmlReport();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI("file:///" + outputPath + "/html/index.html"));
            }
        } catch (IOException e) {
            System.err.println("An error occurred while gathering data from the repository.");
        } catch (URISyntaxException e) {
            System.err.println("Could not open the browser, please open the index.html file: " + outputPath + "/html/index.html");
        }
    }
}
