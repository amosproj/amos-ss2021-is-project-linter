package amosproj.server.linter.checks;

import amosproj.server.data.CheckResultRepository;
import amosproj.server.data.LintingResult;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckGitlabFiles extends Check {

    public CheckGitlabFiles(GitLabApi api, Project project, LintingResult lintingResult, CheckResultRepository checkResultRepository) {
        super(api, project, lintingResult, checkResultRepository);
    }

    /////////////////
    ///// TESTS /////
    /////////////////

    public boolean checkReadmeExistence() {
        return project.getReadmeUrl() != null;
    }

    public boolean checkContributingExistence() {
        return fileExists("CONTRIBUTING.md");
    }

    public boolean checkMaintainersExistence() {
        return fileExists("MAINTAINERS.md");
    }

    public boolean fileExists(String filepath) {
        try {
            RepositoryFile file = api.getRepositoryFileApi().getFileInfo(project.getId(), filepath, project.getDefaultBranch());
            if (file == null) return false;
        } catch (GitLabApiException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public boolean checkNoContributingChain() {
        // generiere regex
        final var regex = "(?i)(?>https?://)?(?>www.)?(?>[a-zA-Z0-9]+)\\.[a-zA-Z0-9]*\\.[a-z]{3}.*/contributing.md";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        boolean found = false;
        //lade Datei in java.io.tmp
        File file = getRawFile("CONTRIBUTING.md");
        if (file != null) {
            try {
                Scanner scanner = new Scanner(file);
                //lese Zeile der Datei bis Ende
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // führe regex auf zeile aus
                    final Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        found = true;
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        // ergebnis umdrehen weil wir auf NO CHAIN prüfen
        return !found;
    }

    public boolean checkReadmeHasLinks() {
        // generiere regex
        final var regex = "(?i)(?>https?://)?online.bk.datev.de/documentation.*";
        // TODO: ADD REGEX FOR CONFLUENCE LINKS
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        boolean found = false;

        //lade Datei in java.io.tmp
        // TODO: adjust this to work with project.getReadmeUrl() instead of README.md
        File file = getRawFile("README.md");
        if (file != null) {
            try {
                Scanner scanner = new Scanner(file);
                //lese Zeile der Datei bis Ende
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // führe regex auf zeile aus
                    final Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        found = true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return found;
    }

    public File getRawFile(String filepath) {
        if (fileExists(filepath)) {
            try {
                //lade die Datei nach java.io.tmp
                return api.getRepositoryFileApi().getRawFile(project.getId(), project.getDefaultBranch(), filepath, null);
            } catch (GitLabApiException e) {
                System.out.println("reason: " + e.getReason());
            }
        }
        return null;
    }

}
