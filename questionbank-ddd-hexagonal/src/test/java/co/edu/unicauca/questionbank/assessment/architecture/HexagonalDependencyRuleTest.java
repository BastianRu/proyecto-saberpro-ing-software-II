package co.edu.unicauca.questionbank.assessment.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the dependency rule of the hexagon by reading the source files: the
 * core (domain and application) must not import Spring, JPA, the web adapter or
 * the Workshop 10 module. Only the adapters may know those technologies.
 */
class HexagonalDependencyRuleTest {

    private static final Path ROOT = Path.of("src/main/java/co/edu/unicauca/questionbank/assessment");

    private static List<String> violations(Path folder, List<String> forbidden) throws IOException {
        List<String> found = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ROOT.resolve(folder))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                for (String line : Files.readAllLines(file)) {
                    if (line.startsWith("import ") && forbidden.stream().anyMatch(line::contains)) {
                        found.add(ROOT.relativize(file) + " -> " + line);
                    }
                }
            }
        }
        return found;
    }

    @Test
    void domainIsPureJava() throws IOException {
        List<String> found = violations(Path.of("domain"), List.of("org.springframework", "jakarta.",
                "co.edu.unicauca.questionbank.domain.", "assessment.application", "assessment.adapter"));
        assertTrue(found.isEmpty(), "Domain must be pure Java: " + found);
    }

    @Test
    void applicationCoreDependsOnlyOnPortsAndDomain() throws IOException {
        List<String> found = violations(Path.of("application"), List.of("org.springframework", "jakarta.",
                "co.edu.unicauca.questionbank.domain.", "assessment.adapter"));
        assertTrue(found.isEmpty(), "Application core must not know adapters or frameworks: " + found);
    }
}
