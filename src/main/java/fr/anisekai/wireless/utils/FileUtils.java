package fr.anisekai.wireless.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

/**
 * Utility class for various {@link File} operation.
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * Check if the given {@code child} is an immediate subdirectory or file directly inside {@code parent}.
     *
     * @param parent
     *         The base directory
     * @param child
     *         The file or directory to test
     *
     * @return True if {@code child} is located directly within {@code parent}, false otherwise
     */
    public static boolean isDirectChild(File parent, File child) {

        Path parentPath = parent.toPath().toAbsolutePath().normalize();
        Path childPath  = child.toPath().toAbsolutePath().normalize();

        return childPath.getParent().equals(parentPath);
    }

    /**
     * Check if the given {@code child} is located anywhere under the {@code parent} directory.
     *
     * @param parent
     *         The base directory
     * @param child
     *         The file or directory to test
     *
     * @return True if {@code child} is a descendant of {@code parent}, false otherwise
     */
    public static boolean isChild(File parent, File child) {

        Path parentPath = parent.toPath().toAbsolutePath().normalize();
        Path childPath  = child.toPath().toAbsolutePath().normalize();

        return childPath.startsWith(parentPath);
    }

    /**
     * Recursively deletes the provided {@link File}. If it's a directory, its content will be deleted first. Fails fast on errors
     * and does not follow symbolic links to avoid unintended deletion.
     *
     * @param file
     *         The {@link File} or directory to delete.
     *
     * @throws IOException
     *         If any deletion fails
     */
    public static void deleteRecursively(File file) throws IOException {

        if (!file.exists()) {
            return;
        }

        Path path = file.toPath();

        if (Files.isSymbolicLink(path)) {
            // Delete the link itself, not what it points to
            Files.delete(path);
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                throw new IOException("Unable to list directory: " + file);
            }

            for (File child : files) {
                deleteRecursively(child);
            }
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    /**
     * Copy recursively a {@link Path} to another {@link Path}.
     *
     * @param source
     *         The source {@link Path}
     * @param destination
     *         The destination {@link Path}
     * @param options
     *         An array of {@link CopyOption} to use while copying data.
     *
     * @throws IOException
     *         If the copy fails.
     */
    public static void copyRecursively(Path source, Path destination, CopyOption... options) throws IOException {

        Files.walkFileTree(
                source,
                new SimpleFileVisitor<>() {

                    @Override
                    public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {

                        Files.createDirectories(destination.resolve(source.relativize(dir).toString()));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {

                        Files.copy(file, destination.resolve(source.relativize(file).toString()), options);
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    /**
     * Ensure that the provided {@link File} will be an existing directory.
     *
     * @param directory
     *         The {@link File} pointing to a directory.
     *
     * @throws IOException
     *         If the directory could not be created, or if it was a file all along.
     */
    public static void ensureDirectoryExists(File directory) throws IOException {

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create directory (" + directory.getAbsolutePath() + ")");
            }
        }

        if (directory.isFile()) {
            throw new IOException("Not a directory (" + directory.getAbsolutePath() + ")");
        }
    }

    /**
     * Ensure that the provided {@link File} will be an existing directory.
     *
     * @param directory
     *         The {@link File} pointing to a directory.
     * @param exceptionWrapper
     *         The {@link Function} to use to wrap an {@link IOException} if it ever occurs.
     * @param <T>
     *         The exception type thrown
     */
    public static <T extends RuntimeException> void ensureDirectoryExists(File directory, Function<IOException, T> exceptionWrapper) {

        try {
            ensureDirectoryExists(directory);
        } catch (IOException e) {
            throw exceptionWrapper.apply(e);
        }
    }

}
