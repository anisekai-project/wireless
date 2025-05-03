package fr.anisekai.wireless.persistence.data;

import java.time.ZonedDateTime;

public class TestEntity implements ITestEntity {

    private long   id;
    private String name;

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public Long getId() {

        return this.id;
    }

    @Override
    public ZonedDateTime getCreatedAt() {

        return null;
    }

    @Override
    public ZonedDateTime getUpdatedAt() {

        return null;
    }

    @Override
    public boolean isNew() {

        return false;
    }

}
