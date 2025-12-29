package io.github.shamsu07.nomos.core.source;

import io.github.shamsu07.nomos.core.loader.YAMLRuleLoader;
import io.github.shamsu07.nomos.core.reload.FileWatcher;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Loads rules from filesystem (single file or directory of YAML files).
 *
 * <p>Supports classpath: prefix for bundled resources and absolute/relative paths for external
 * files. When pointing to a directory, loads all .yml and .yaml files.
 */
public final class FileSystemRuleSource implements RuleSource {

  private final String path;
  private final YAMLRuleLoader yamlLoader;
  private final FileWatcher fileWatcher;
  private volatile Path resolvedPath;

  public FileSystemRuleSource(String path, YAMLRuleLoader yamlLoader) throws IOException {
    this.path = Objects.requireNonNull(path, "Path cannot be null");
    this.yamlLoader = Objects.requireNonNull(yamlLoader, "YAMLRuleLoader cannot be null");
    this.fileWatcher = new FileWatcher();
  }

  @Override
  public List<Rule> loadRules() throws IOException {
    // Resolve path on first load (Cache for hot reload)
    if (resolvedPath == null) {
      resolvedPath = resolvePath(path);
    }

    List<Path> yamlFiles = scanYamlFiles(resolvedPath);
    List<Rule> allRules = new ArrayList<>(yamlFiles.size() * 10); // Pre-size estimate

    for (Path file : yamlFiles) {
      try (InputStream inputStream = Files.newInputStream(file)) {
        allRules.addAll(yamlLoader.load(inputStream));
      } catch (Exception e) {
        throw new IOException("Failed to load rules from " + file + ": " + e.getMessage(), e);
      }
    }

    return allRules;
  }

  @Override
  public void enableHotReload(Runnable reloadCallback) throws IOException {
    Objects.requireNonNull(reloadCallback, "Reload callback cannot be null");

    if (resolvedPath == null) {
      throw new IllegalStateException("Must call loadRules() before enabling hot reload");
    }

    if (!Files.exists(resolvedPath)) {
      throw new IOException("Path does not exist: " + resolvedPath);
    }

    if (Files.isDirectory(resolvedPath)) {
      fileWatcher.watchDirectory(resolvedPath, reloadCallback);
    } else {
      fileWatcher.watch(resolvedPath, reloadCallback);
    }
  }

  @Override
  public void close() {
    fileWatcher.close();
  }

  private Path resolvePath(String path) throws IOException {
    // Handle classpath: prefix
    if (path.startsWith("classpath:")) {
      String resourcePath = path.substring("classpath:".length());

      var url = getClass().getClassLoader().getResource(resourcePath);
      if (url == null) {
        throw new IOException("Classpath resource not found: " + resourcePath);
      }

      try {
        return Paths.get(url.toURI());
      } catch (Exception e) {
        throw new IOException("Cannot resolve classpath resource: " + resourcePath, e);
      }
    }

    // Handle file: prefix
    if (path.startsWith("file:")) {
      try {
        return Paths.get(URI.create(path));
      } catch (Exception e) {
        throw new IOException("Invalid file URI: " + path, e);
      }
    }

    // Regular file path (absolute or relative)
    Path filePath = Paths.get(path);
    if (!Files.exists(filePath)) {
      throw new IOException("File or directory not found: " + path);
    }
    return filePath;
  }

  private List<Path> scanYamlFiles(Path path) throws IOException {
    // Single file
    if (Files.isRegularFile(path)) {
      return List.of(path);
    }

    // Directory - scan for .yml and .yaml files (depth 1 only, no recursion)
    if (Files.isDirectory(path)) {
      try (Stream<Path> paths = Files.walk(path, 1)) {
        return paths
            .filter(Files::isRegularFile)
            .filter(this::isYamlFile)
            .sorted() // Consistent ordering
            .toList();
      }
    }

    throw new IOException("Path is neither file nor directory: " + path);
  }

  private boolean isYamlFile(Path path) {
    String filename = path.getFileName().toString().toLowerCase();
    return filename.endsWith(".yml") || filename.endsWith(".yaml");
  }
}
