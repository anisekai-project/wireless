package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.exceptions.StorageAccessException;
import fr.anisekai.wireless.api.storage.exceptions.StorageOutOfBoundException;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageAware;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;
import fr.anisekai.wireless.utils.FileUtils;
import fr.anisekai.wireless.utils.FlowUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link StoragePathResolver} allows to resolve any {@link Path} without permission or scope checks. This effectively allows to
 * separate the {@link Path} resolution from the {@link AccessScope} allowing each implementation to handle permission or checks
 * as they see fit before any real resolution, while still allowing them to resolve {@link Path} internally without writing check
 * bypass code.
 *
 * @param root
 *         The root {@link Path} from which all resolution will occur.
 */
public record StoragePathResolver(Path root) implements StorageAware {

    /**
     * Create a new {@link StoragePathResolver}.
     *
     * @param root
     *         The {@link Path} pointing toward this {@link StorageAware} root.
     */
    public StoragePathResolver(Path root) {

        this.root = root.toAbsolutePath().normalize();
    }

    static Path walkDirectory(Path root, String destination, Function<Path, StorageOutOfBoundException> oobSupplier) {

        Path walked = root.resolve(destination).toAbsolutePath().normalize();

        if (!walked.startsWith(root)) {
            throw oobSupplier.apply(walked);
        }

        FlowUtils.wrapException(() -> FileUtils.ensureDirectory(walked), StorageAccessException::new);
        return walked;
    }

    private static Path walkFile(Path root, String destination, Function<Path, StorageOutOfBoundException> oobSupplier) {

        Path walked = root.resolve(destination).toAbsolutePath().normalize();
        FlowUtils.wrapException(() -> FileUtils.ensureFile(walked), StorageAccessException::new);

        if (!walked.startsWith(root)) {
            throw oobSupplier.apply(walked);
        }

        return walked;
    }

    private Path internalResolveFile(StorageStore store, String filename, boolean isScoped) {

        if (store.type().isEntityScoped() != isScoped) {
            throw new StorageAccessException(String.format(
                    "Tried to access store '%s' as a %s store.",
                    store.name(),
                    isScoped ? "scoped" : "raw"
            ));
        }

        return walkFile(
                this.resolveDirectory(store),
                filename,
                walked -> new StorageOutOfBoundException(String.format(
                        "Resolved file for store '%s' with name '%s' is out-of-bound (Resolved: %s)",
                        store.name(),
                        filename,
                        walked
                ))
        );
    }

    @Override
    public String name() {

        return this.root().getFileName().toString();
    }

    @Override
    public Path root() {

        return this.root.toAbsolutePath().normalize();
    }

    @Override
    public Path resolveDirectory(StorageStore store) {

        return walkDirectory(
                this.root(),
                store.name(),
                walked -> new StorageOutOfBoundException(String.format(
                        "Resolved directory for store '%s' is out-of-bound (Resolved: %s)",
                        store.name(),
                        walked
                ))
        );
    }

    @Override
    public Path resolveDirectory(StorageStore store, ScopedEntity entity) {

        if (!store.type().isEntityScoped()) {
            throw new StorageAccessException(String.format(
                    "Tried to access store '%s' as a scoped store.",
                    store.name()
            ));
        }

        return walkDirectory(
                this.resolveDirectory(store),
                entity.getScopedName(),
                walked -> new StorageOutOfBoundException(String.format(
                        "Resolved directory for store '%s' with entity '%s' is out-of-bound (Resolved: %s)",
                        store.name(),
                        entity.getScopedName(),
                        walked
                ))
        );
    }

    @Override
    public Path resolveFile(StorageStore store, String filename) {

        return this.internalResolveFile(store, filename, false);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity) {

        String filename = String.format("%s.%s", entity.getScopedName(), store.extension());
        return this.internalResolveFile(store, filename, true);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity, String filename) {

        if (!store.type().isEntityScoped()) {
            throw new StorageAccessException(String.format(
                    "Tried to access store '%s' as a scoped store.",
                    store.name()
            ));
        }

        return walkFile(
                this.resolveDirectory(store, entity),
                filename,
                walked -> new StorageOutOfBoundException(String.format(
                        "Resolved file for store '%s' with entity '%s' and name '%s' is out-of-bound (Resolved: %s)",
                        store.name(),
                        entity.getScopedName(),
                        filename,
                        walked
                ))
        );
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StoragePathResolver) obj;
        return Objects.equals(this.root, that.root);
    }

    @Override
    public @NotNull String toString() {

        return "StoragePathResolver[root=" + this.root + ']';
    }

}
