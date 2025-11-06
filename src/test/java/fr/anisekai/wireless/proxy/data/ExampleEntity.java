package fr.anisekai.wireless.proxy.data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ExampleEntity implements IExampleEntity {

    public static ExampleEntity create() {

        return create(1L);
    }

    public static ExampleEntity create(long id) {

        ExampleEntity entity = new ExampleEntity();
        entity.setId(id);
        entity.setActive(true);
        entity.setTags(null);
        entity.setMapping(null);
        entity.setIgnored(true);
        return entity;
    }


    private Long                id;
    private String              name;
    private boolean             active;
    private List<String>        tags;
    private Map<String, String> mapping;
    private boolean             ignoreThis;
    private ExampleEntity       entity;

    @Override
    public Long getId() {

        return this.id;
    }

    @Override
    public void setId(Long id) {

        this.id = id;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean isActive() {

        return this.active;
    }

    @Override
    public void setActive(boolean active) {

        this.active = active;
    }

    @Override
    public List<String> getTags() {

        return this.tags;
    }

    @Override
    public void setTags(List<String> tags) {

        this.tags = tags;
    }

    @Override
    public Map<String, String> getMapping() {

        return this.mapping;
    }

    @Override
    public void setMapping(Map<String, String> mapping) {

        this.mapping = mapping;
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

        return this.id == null;
    }

    public boolean isIgnored() {

        return this.ignoreThis;
    }

    public void setIgnored(boolean ignoreThis) {

        this.ignoreThis = ignoreThis;
    }

    public void setIgnoreThis(boolean ignoreThis) {

        this.ignoreThis = ignoreThis;
    }

    public ExampleEntity getEntity() {

        return this.entity;
    }

    public void setEntity(ExampleEntity entity) {

        this.entity = entity;
    }

    @Override
    public String toString() {

        return "ExampleEntity{id=%d, name='%s', active=%s, tags=%s, mapping=%s, ignoreThis=%s, entity=%s}".formatted(
                this.id,
                this.name,
                this.active,
                this.tags,
                this.mapping,
                this.ignoreThis,
                this.entity
        );
    }

}
