package fr.anisekai.wireless.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
    public static boolean isDirectChild(Path parent, Path child) {

        Path parentPath = parent.toAbsolutePath().normalize();
        Path childPath  = child.toAbsolutePath().normalize();

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
    public static boolean isChild(Path parent, Path child) {

        Path parentPath = parent.toAbsolutePath().normalize();
        Path childPath  = child.toAbsolutePath().normalize();

        return childPath.startsWith(parentPath);
    }

    /**
     * Recursively deletes the provided {@link Path}. If it's a directory, its content will be deleted first.
     *
     * @param path
     *         The {@link Path} of the directory or file to delete.
     *
     * @throws IOException
     *         If any deletion fails
     */
    public static void delete(Path path) throws IOException {

        if (!Files.exists(path)) {
            return;
        }

        if (Files.isRegularFile(path)) {
            Files.delete(path);
            return;
        }

        if (Files.isDirectory(path)) {
            Files.walkFileTree(
                    path,
                    new SimpleFileVisitor<>() {

                        @Override
                        public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {

                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, @Nullable IOException exc) throws IOException {

                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
            return;
        }

        throw new UnsupportedOperationException("Unable to delete path: " + path);
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
    public static void copy(Path source, Path destination, CopyOption... options) throws IOException {

        if (Files.isDirectory(source)) {
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
            return;
        }

        if (Files.isRegularFile(source)) {
            Files.copy(source, destination, options);
            return;
        }

        throw new UnsupportedOperationException("Unable to copy source file: " + source);
    }

    /**
     * Make sure the provided {@link Path} is a directory.
     *
     * @param path
     *         The {@link Path}
     *
     * @throws IOException
     *         If the directory could not be created.
     */
    public static void ensureDirectory(Path path) throws IOException {

        if (Files.isDirectory(path)) {
            return;
        }

        if (Files.isRegularFile(path)) {
            throw new IllegalStateException(path + " is a regular file");
        }

        if (!Files.exists(path)) {
            Files.createDirectories(path);
            return;
        }

        throw new IllegalStateException(path + " exists and it is neither a directory or regular file");
    }

    /**
     * Make sure the provided {@link Path} is a file.
     *
     * @param path
     *         The {@link Path}
     */
    public static void ensureFile(Path path) {

        if (!Files.exists(path) || Files.isRegularFile(path)) {
            return;
        }

        throw new IllegalStateException(path + " is a not a file");
    }

}
