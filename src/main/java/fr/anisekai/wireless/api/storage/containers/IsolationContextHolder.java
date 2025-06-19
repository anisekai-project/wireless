package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;

import java.io.File;
import java.util.Objects;
import java.util.Set;

/**
 * Record holding the data related to a {@link FileIsolationContext} and its {@link AccessScope}.
 */
public final class IsolationContextHolder {

    private final String               name;
    private final File                 root;
    private final Set<AccessScope>     scopes;
    private final FileIsolationContext isolation;
    private       boolean              committed = false;

    /**
     * Create a new {@link IsolationContextHolder}
     *
     * @param name
     *         The name of the {@link FileIsolationContext}.
     * @param root
     *         {@link File} into which the {@link FileIsolationContext} is active.
     * @param scopes
     *         A {@link Set} of {@link AccessScope} granted to the {@link AccessScope}
     * @param isolation
     *         The {@link FileIsolationContext}.
     */
    public IsolationContextHolder(String name, File root, Set<AccessScope> scopes, FileIsolationContext isolation) {

        this.name      = name;
        this.root      = root;
        this.scopes    = scopes;
        this.isolation = isolation;
    }

    /**
     * Retrieve this {@link IsolationContextHolder}'s name.
     *
     * @return A name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Retrieve this {@link IsolationContextHolder}'s {@link File} pointing toward its {@link FileIsolationContext} storage
     * directory.
     *
     * @return A {@link File}
     */
    public File getRoot() {

        return this.root;
    }

    /**
     * Retrieve this {@link IsolationContextHolder}'s set of claimed {@link AccessScope}.
     *
     * @return A set of {@link AccessScope}.
     */
    public Set<AccessScope> getScopes() {

        return this.scopes;
    }

    /**
     * Retrieve the {@link FileIsolationContext} for which this {@link IsolationContextHolder} has been created.
     *
     * @return A {@link FileIsolationContext}.
     */
    public FileIsolationContext getIsolation() {

        return this.isolation;
    }

    /**
     * Check if this {@link IsolationContextHolder}'s {@link FileIsolationContext} has already been committed.
     *
     * @return True if the {@link FileIsolationContext} has been committed, false otherwise.
     */
    public boolean isCommitted() {

        return this.committed;
    }

    /**
     * Define if this {@link IsolationContextHolder}'s {@link FileIsolationContext} has been committed.
     *
     * @param committed
     *         True if the {@link FileIsolationContext} has been committed, false otherwise.
     */
    public void setCommitted(boolean committed) {

        this.committed = committed;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IsolationContextHolder) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.root, that.root) &&
                Objects.equals(this.scopes, that.scopes) &&
                Objects.equals(this.isolation, that.isolation);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.name, this.root, this.scopes, this.isolation);
    }

    @Override
    public String toString() {

        return String.format(
                "IsolationContextHolder{name='%s', root=%S, isolation=%s, committed=%s}",
                this.name,
                this.root,
                this.isolation,
                this.committed
        );
    }

}
