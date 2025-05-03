package fr.anisekai.wireless.persistence.data;

import fr.anisekai.wireless.api.persistence.TriggerEvent;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;

public interface ITestEntity extends Entity<Long> {

    String getName();

    @TriggerEvent(TestNameUpdatedEvent.class)
    void setName(String name);

}
