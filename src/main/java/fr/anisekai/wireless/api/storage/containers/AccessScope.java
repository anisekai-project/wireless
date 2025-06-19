package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.exceptions.ScopeDefinitionException;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represent an access scope within a {@link StorageStore} granted for a {@link StorageIsolationContext}.
 */
public final class AccessScope {

    private static final Pattern CLAIM_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    private final StorageStore storageStore;
    private final ScopedEntity claim;

    /**
     * Create a new {@link AccessScope} on the provided {@link StorageStore} targeting the {@link ScopedEntity}.
     *
     * @param storageStore
     *         The {@link StorageStore} on which the scope is effective.
     * @param entity
     *         The {@link ScopedEntity} on which the scope is effective.
     */
    public AccessScope(@NotNull StorageStore storageStore, @NotNull ScopedEntity entity) {

        this.storageStore = storageStore;
        this.claim        = entity;

        if (!this.getStore().type().isEntityScoped()) {
            throw new ScopeDefinitionException(String.format(
                    "Scopes cannot be used with '%s' store, as it is a non-scoped store.",
                    this.getStore().name()
            ));
        }

        if (!this.getStore().entityClass().equals(entity.getClass())) {
            throw new ScopeDefinitionException(String.format(
                    "Cannot create a scope on store '%s' using entity type '%s' (expecting type '%s')",
                    this.getStore().name(),
                    entity.getClass().getSimpleName(),
                    this.getStore().entityClass().getSimpleName()
            ));
        }

        if (!CLAIM_PATTERN.matcher(this.claim.getScopedName()).matches()) {
            throw new ScopeDefinitionException(String.format(
                    "Scoped entity name '%s' does not match expected format %s",
                    this.claim.getScopedName(),
                    CLAIM_PATTERN.pattern()
            ));
        }
    }

    /**
     * Retrieve the {@link StorageStore} on which this {@link AccessScope} is effective.
     *
     * @return A {@link StorageStore}
     */
    public StorageStore getStore() {

        return this.storageStore;
    }

    /**
     * Retrieve the {@link ScopedEntity} within the {@link StorageStore} on which this {@link AccessScope} is effective.
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
        return Objects.equals(this.storageStore, that.storageStore) &&
                Objects.equals(this.claim.getClass(), that.claim.getClass()) &&
                Objects.equals(this.claim.getScopedName(), that.claim.getScopedName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.storageStore, this.claim.getClass(), this.claim.getScopedName());
    }

    @Override
    public String toString() {

        return String.format("Scope[%s:%s]", this.storageStore.name(), this.claim.getScopedName());
    }

}
