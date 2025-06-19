package fr.anisekai.wireless.storage.data;

import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import org.jetbrains.annotations.NotNull;

public class ScopedEntityA implements ScopedEntity {

    private final String name;

    public ScopedEntityA(String name) {

        this.name = name;
    }

    @Override
    public @NotNull String getScopedName() {

        return this.name;
    }

}
