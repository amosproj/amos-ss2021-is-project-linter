package amosproj.server.linter;

import amosproj.server.GitLab;
import amosproj.server.data.*;
import amosproj.server.linter.checks.CheckGitlabFiles;
import amosproj.server.linter.checks.CheckGitlabRoles;
import amosproj.server.linter.checks.CheckGitlabSettings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Linter führt den tatsächlichen Lint-Vorgang durch.
 * Weiterhin stellt sie eine Methode zur Verfügung, um die config-Datei einzulesen.
 */
@Service
public class Linter {

    // autowired
    private final GitLab api;
    private final LintingResultRepository lintingResultRepository;
    private final CheckResultRepository checkResultRepository;
    private final ProjectRepository projectRepository;
    // end autowired

    public Linter(GitLab api, LintingResultRepository lintingResultRepository, CheckResultRepository checkResultRepository, ProjectRepository projectRepository) {
        this.api = api;
        this.lintingResultRepository = lintingResultRepository;
        this.checkResultRepository = checkResultRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * entry point to start asynchronous linting process
     *
     * @param repoUrl
     * @throws GitLabApiException
     */
    public void runLint(String repoUrl) throws GitLabApiException {
        repoUrl = repoUrl.replace("\r", "");
        String path = repoUrl.replace(api.getGitlabHost() + "/", "");

        org.gitlab4j.api.models.Project proj = api.getApi().getProjectApi().getProject(path);
        assert proj != null;
        checkEverything(proj, LocalDateTime.now());
    }

    /**
     * internal method that starts all checks, and saves the results
     *
     * @param apiProject
     */
    public void checkEverything(org.gitlab4j.api.models.Project apiProject, LocalDateTime timestamp) {
        // Hole LintingProject
        Project currLintingProject = projectRepository.findFirstByGitlabProjectId(apiProject.getId());
        if (currLintingProject == null) {
            // Erstelle neues Projekt mit Description und ForkCount
            currLintingProject = new Project(apiProject.getName(), apiProject.getWebUrl(), apiProject.getId(), api.getGitlabHost(), apiProject.getDescription(), apiProject.getForksCount(), apiProject.getLastActivityAt());
            projectRepository.save(currLintingProject);
        } else {
            // Update Description, LastActivity und ForkCount
            currLintingProject.setDescription(apiProject.getDescription());
            currLintingProject.setForkCount(apiProject.getForksCount());
            currLintingProject.setLastCommit(apiProject.getLastActivityAt());
        }

        // Erstelle neues Linting Result
        LintingResult lintingResult = new LintingResult(currLintingProject, timestamp);
        lintingResultRepository.save(lintingResult);

        // Fuehre Checks aus
        JsonNode checks = getConfigNode().get("checks");

        var fileChecks = new CheckGitlabFiles(api, apiProject, lintingResult, checkResultRepository);
        var settingsChecks = new CheckGitlabSettings(api, apiProject, lintingResult, checkResultRepository);
        var roleChecks = new CheckGitlabRoles(api, apiProject, lintingResult, checkResultRepository);

        for (Iterator<String> it = checks.fieldNames(); it.hasNext(); ) {
            String testName = it.next();
            JsonNode check = checks.get(testName);
            switch (check.get("category").asText()) {
                case "file_checks":
                    fileChecks.runTest(testName, check);
                    break;
                case "settings_checks":
                    settingsChecks.runTest(testName, check);
                    break;
                case "role_checks":
                    roleChecks.runTest(testName, check);
                    break;
            }
        }

    }


    /**
     * Gets the config.json and parses it into a JsonNode
     *
     * @return JsonNode of the parsed config.json
     */
    public static JsonNode getConfigNode() {
        ClassPathResource file = new ClassPathResource("config.json");
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        JsonNode node = null;
        try {
            node = objectMapper.readTree(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

}
