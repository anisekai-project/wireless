package fr.anisekai.wireless.proxy.data;

import fr.anisekai.wireless.interfaces.Entity;

import java.util.List;
import java.util.Map;

public interface IExampleEntity extends Entity<Long> {

    String getName();

    void setName(String name);

    boolean isActive();

    void setActive(boolean active);

    List<String> getTags();

    void setTags(List<String> tags);

    Map<String, String> getMapping();

    void setMapping(Map<String, String> mapping);

}
