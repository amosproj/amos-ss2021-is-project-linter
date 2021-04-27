package amosproj.server.data;

import org.gitlab4j.api.utils.JacksonJson;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class FileCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long lintId;
    private String fileName;
    private Boolean exists;

    public FileCheck(LintingResult lintingResults, String fileName, Boolean exists) {
        this.lintId = lintingResults.getId();
        this.fileName = fileName;
        this.exists = exists;
    }

    protected FileCheck() {
    }


    public Long getId() {
        return id;
    }

    public Long getLintId() {
        return lintId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    @Override
    public String toString() {
        return JacksonJson.toJsonString(this);
    }

}
