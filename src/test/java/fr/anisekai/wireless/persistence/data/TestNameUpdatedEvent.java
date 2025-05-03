package fr.anisekai.wireless.persistence.data;

import fr.anisekai.wireless.api.persistence.interfaces.EntityUpdatedEvent;

import java.util.Objects;

public final class TestNameUpdatedEvent implements EntityUpdatedEvent<TestEntity, String> {

    private       TestEntity entity;
    private final String     oldValue;
    private final String     newValue;

    public TestNameUpdatedEvent(
            Object source,
            TestEntity entity,
            String oldValue,
            String newValue
    ) {

        this.entity   = entity;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public TestEntity getEntity() {

        return this.entity;
    }

    @Override
    public void setEntity(TestEntity entity) {

        this.entity = entity;
    }

    @Override
    public String getOldValue() {

        return this.oldValue;
    }

    @Override
    public String getNewValue() {

        return this.newValue;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TestNameUpdatedEvent) obj;
        return Objects.equals(this.entity, that.entity) &&
                Objects.equals(this.oldValue, that.oldValue) &&
                Objects.equals(this.newValue, that.newValue);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.entity, this.oldValue, this.newValue);
    }

    @Override
    public String toString() {

        return "TestNameUpdatedEvent[" +
                "getEntity=" + this.entity + ", " +
                "getOldValue=" + this.oldValue + ", " +
                "getNewValue=" + this.newValue + ']';
    }


}
