package amosproj.server.data;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Das LintingResultRepository ist das Interface zur Datenbank für die LintingResults.
 * Hier werden die Datenbank-Queries ausgeführt.
 * Der Name der Methoden ist die SQL query.
 */
public interface LintingResultRepository extends CrudRepository<LintingResult, Long> {

    /**
     * @param projectId Foreign-Key für das zugehörige Project
     * @return das neueste LintingResult
     */
    LintingResult findFirstByProjectIdOrderByLintTimeDesc(Long projectId);

    /**
     * @param start     Startzeitpunkt
     * @param end       Endzeitpunkt
     * @param projectId Foreign-Key für das zugehörige Project
     * @return eine Liste an LintingResults die zwischen start und end liegen
     */
    List<LintingResult> findByLintTimeBetweenAndProjectIdIs(LocalDateTime start, LocalDateTime end, Long projectId);
}
