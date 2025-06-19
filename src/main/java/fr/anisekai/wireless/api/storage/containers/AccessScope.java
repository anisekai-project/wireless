package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.exceptions.ScopeDefinitionException;
import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represent an access scope within a {@link FileStore} granted for a {@link FileIsolationContext}.
 */
public final class AccessScope {

    private static final Pattern CLAIM_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    private final FileStore    fileStore;
    private final ScopedEntity claim;

    /**
     * Create a new {@link AccessScope} on the provided {@link FileStore} targeting the {@link ScopedEntity}.
     *
     * @param fileStore
     *         The {@link FileStore} on which the scope is effective.
     * @param entity
     *         The {@link ScopedEntity} on which the scope is effective.
     */
    public AccessScope(@NotNull FileStore fileStore, @NotNull ScopedEntity entity) {

        this.fileStore = fileStore;
        this.claim     = entity;

        if (!this.getFileStore().type().isEntityScoped()) {
            throw new ScopeDefinitionException(String.format(
                    "Scopes cannot be used with '%s' store, as it is a non-scoped store.",
                    this.getFileStore().name()
            ));
        }

        if (!this.getFileStore().entityClass().equals(entity.getClass())) {
            throw new ScopeDefinitionException(String.format(
                    "Cannot create a scope on store '%s' using entity type '%s' (expecting type '%s')",
                    this.getFileStore().name(),
                    entity.getClass().getSimpleName(),
                    this.getFileStore().entityClass().getSimpleName()
            ));
        }

        if (!CLAIM_PATTERN.matcher(this.claim.getScopedName()).matches()) {
            throw new ScopeDefinitionException(String.format(
                    "Scoped entity name '%s' does not match expected format %s",
                    this.getFileStore().name(),
                    CLAIM_PATTERN.pattern()
            ));
        }
    }

    /**
     * Retrieve the {@link FileStore} on which this {@link AccessScope} is effective.
     *
     * @return A {@link FileStore}
     */
    public FileStore getFileStore() {

        return this.fileStore;
    }

    /**
     * Retrieve the {@link ScopedEntity} within the {@link FileStore} on which this {@link AccessScope} is effective.
     *
     * @return An {@link ScopedEntity}.
     */
    public ScopedEntity getClaim() {

        return this.claim;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AccessScope) obj;
        return Objects.equals(this.fileStore, that.fileStore) &&
                Objects.equals(this.claim.getClass(), that.claim.getClass()) &&
                Objects.equals(this.claim.getScopedName(), that.claim.getScopedName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.fileStore, this.claim);
    }

    @Override
    public String toString() {

        return String.format("Scope[%s:%s]", this.fileStore.name(), this.claim.getScopedName());
    }

}
