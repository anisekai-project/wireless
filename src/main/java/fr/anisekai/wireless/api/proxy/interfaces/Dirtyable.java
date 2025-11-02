package fr.anisekai.wireless.api.proxy.interfaces;

public interface Dirtyable {

    /**
     * Checks if the state of the object has been modified.
     *
     * @return {@code true} if there are any changes, {@code false} otherwise.
     */
    boolean isDirty();

}
