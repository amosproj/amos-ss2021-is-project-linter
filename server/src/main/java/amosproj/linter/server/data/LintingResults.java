package amosproj.linter.server.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class LintingResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String repoLink;
    private String maintainer;
    private LocalDateTime lastLint;
    private Long fileCheckId;

    public LintingResults(Long id, String repoLink, String maintainer, LocalDateTime lastLint, Long fileCheckId) {
        this.id = id;
        this.repoLink = repoLink;
        this.maintainer = maintainer;
        this.lastLint = lastLint;
        this.fileCheckId = fileCheckId;
    }

    protected LintingResults() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoLink() {
        return repoLink;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public LocalDateTime getLastLint() {
        return lastLint;
    }

    public void setLastLint(LocalDateTime lastLint) {
        this.lastLint = lastLint;
    }

    public Long getFileCheckId() {
        return fileCheckId;
    }

    public void setFileCheckId(Long fileCheckId) {
        this.fileCheckId = fileCheckId;
    }
}