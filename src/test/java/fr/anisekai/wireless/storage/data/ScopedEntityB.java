package fr.anisekai.wireless.storage.data;

import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import org.jetbrains.annotations.NotNull;

public class ScopedEntityB implements ScopedEntity {

    private final String name;

    public ScopedEntityB(String name) {

        this.name = name;
    }

    @Override
    public @NotNull String getScopedName() {

        return this.name;
    }

}
