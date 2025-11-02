package fr.anisekai.wireless.proxy;

import fr.anisekai.wireless.api.proxy.ClassProxyFactory;
import fr.anisekai.wireless.api.proxy.Property;
import fr.anisekai.wireless.api.proxy.interfaces.State;
import fr.anisekai.wireless.proxy.data.ExampleEntity;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@DisplayName("Proxy")
@Tags({@Tag("unit-test"), @Tag("proxy")})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ClassProxyTests {

    @Nested
    @Order(1)
    @DisplayName("Property Scanning")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    class PropertyTests {

        @Test
        @Order(1)
        @DisplayName("Should find all properties")
        void shouldFindAllProperties() {

            Map<String, Property> properties = Property
                    .computeProperties(ExampleEntity.class)
                    .stream()
                    .collect(Collectors.toMap(Property::getName, Function.identity()));

            Assertions.assertEquals(5, properties.size());

            Assertions.assertTrue(properties.containsKey("id"));
            Assertions.assertTrue(properties.containsKey("active"));
            Assertions.assertTrue(properties.containsKey("tags"));
            Assertions.assertTrue(properties.containsKey("mapping"));
            Assertions.assertTrue(properties.containsKey("entity"));
            Assertions.assertFalse(properties.containsKey("ignoreThis"));
            Assertions.assertFalse(properties.containsKey("ignored"));
        }

    }

    @Nested
    @Order(2)
    @DisplayName("Class Proxy Initialization")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    class ClassProxyInitTests {

        @Test
        @Order(1)
        @DisplayName("Should succeed with correct state")
        void shouldInitializeWithoutFailure() {

            Set<Property> properties = Property.computeProperties(ExampleEntity.class);
            ExampleEntity entity     = ExampleEntity.create();

            State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
            Assertions.assertNotNull(stateProxy);

            Map<Property, Object> state = stateProxy.getOriginalState();

            Assertions.assertEquals(properties.size(), state.size());

            Assertions.assertTrue(state.keySet().containsAll(properties));
            Assertions.assertTrue(properties.containsAll(state.keySet()));
        }

    }

    @Nested
    @Order(3)
    @DisplayName("Class Proxy Actions")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    class ClassProxyActionTests {


        @Nested
        @Order(1)
        @DisplayName("On Lists")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        @TestClassOrder(ClassOrderer.OrderAnnotation.class)
        class ProxyListTests {

            @Test
            @Order(1)
            @DisplayName("Should detect change (set) on (null)")
            void shouldDetectChangeFromNull() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity entity = ExampleEntity.create();

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("tags");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                List<String> tags = Arrays.asList("one", "two", "three");
                proxy.setTags(tags);


                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getTags());
                Assertions.assertNotNull(proxy.getTags());

                Assertions.assertEquals(tags.size(), entity.getTags().size());
                Assertions.assertEquals(tags.size(), proxy.getTags().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(2)
            @DisplayName("Should detect change (set) on (non-null)")
            void shouldDetectChangeFromNonNull() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity entity = ExampleEntity.create();
                entity.setTags(new ArrayList<>(List.of("one")));

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("tags");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                List<String> tags = Arrays.asList("one", "two", "three");
                proxy.setTags(tags);

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getTags());
                Assertions.assertNotNull(proxy.getTags());

                Assertions.assertEquals(tags.size(), entity.getTags().size());
                Assertions.assertEquals(tags.size(), proxy.getTags().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(3)
            @DisplayName("Should detect change (add)")
            void shouldDetectInsertion() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity entity = ExampleEntity.create();
                entity.setTags(new ArrayList<>(List.of("one")));

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("tags");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                proxy.getTags().add("two");

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getTags());
                Assertions.assertNotNull(proxy.getTags());

                Assertions.assertEquals(2, entity.getTags().size());
                Assertions.assertEquals(2, proxy.getTags().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(4)
            @DisplayName("Should detect change (remove)")
            void shouldDetectDeletion() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity entity = ExampleEntity.create();
                entity.setTags(new ArrayList<>(List.of("one", "two")));

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("tags");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                proxy.getTags().remove("two");

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getTags());
                Assertions.assertNotNull(proxy.getTags());

                Assertions.assertEquals(1, entity.getTags().size());
                Assertions.assertEquals(1, proxy.getTags().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

        }

        @Nested
        @Order(2)
        @DisplayName("On Maps")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        @TestClassOrder(ClassOrderer.OrderAnnotation.class)
        class ProxyMapTests {

            @Test
            @Order(1)
            @DisplayName("Should detect change (set) on (null)")
            void shouldDetectChangeFromNull() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity entity = ExampleEntity.create();

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("mapping");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                Map<String, String> map = new HashMap<>();
                map.put("one", "1");
                map.put("two", "2");

                proxy.setMapping(map);

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getMapping());
                Assertions.assertNotNull(proxy.getMapping());

                Assertions.assertEquals(map.size(), entity.getMapping().size());
                Assertions.assertEquals(map.size(), proxy.getMapping().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(2)
            @DisplayName("Should detect change (set) on (non-null)")
            void shouldDetectChangeFromNonNull() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity       entity = ExampleEntity.create();
                Map<String, String> map1   = new HashMap<>();
                map1.put("one", "1");
                map1.put("two", "2");
                entity.setMapping(map1);

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("mapping");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                Map<String, String> map2 = new HashMap<>();
                map2.put("one", "1");
                map2.put("two", "2");

                proxy.setMapping(map2);

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getMapping());
                Assertions.assertNotNull(proxy.getMapping());

                Assertions.assertEquals(map2.size(), entity.getMapping().size());
                Assertions.assertEquals(map2.size(), proxy.getMapping().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(3)
            @DisplayName("Should detect change (put)")
            void shouldDetectInsertion() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity       entity = ExampleEntity.create();
                Map<String, String> map    = new HashMap<>();
                map.put("one", "1");
                map.put("two", "2");
                entity.setMapping(map);

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("mapping");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                proxy.getMapping().put("three", "3");

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getMapping());
                Assertions.assertNotNull(proxy.getMapping());

                Assertions.assertEquals(3, entity.getMapping().size());
                Assertions.assertEquals(3, proxy.getMapping().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

            @Test
            @Order(4)
            @DisplayName("Should detect change (remove)")
            void shouldDetectDeletion() {

                Map<String, Property> properties = Property
                        .computeProperties(ExampleEntity.class)
                        .stream()
                        .collect(Collectors.toMap(Property::getName, Function.identity()));

                ExampleEntity       entity = ExampleEntity.create();
                Map<String, String> map    = new HashMap<>();
                map.put("one", "1");
                map.put("two", "2");
                entity.setMapping(map);

                State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
                Assertions.assertNotNull(stateProxy);

                Property      property = properties.get("mapping");
                ExampleEntity proxy    = stateProxy.getProxy();

                Assertions.assertFalse(stateProxy.isDirty());

                proxy.getMapping().remove("two");

                Map<Property, Object> state   = stateProxy.getOriginalState();
                Map<Property, Object> changes = stateProxy.getDifferentialState();

                Assertions.assertFalse(state.isEmpty());
                Assertions.assertFalse(changes.isEmpty());

                Assertions.assertTrue(state.containsKey(property));
                Assertions.assertTrue(changes.containsKey(property));

                Assertions.assertNotNull(entity.getMapping());
                Assertions.assertNotNull(proxy.getMapping());

                Assertions.assertEquals(1, entity.getMapping().size());
                Assertions.assertEquals(1, proxy.getMapping().size());

                Assertions.assertTrue(stateProxy.isDirty());
            }

        }

        @Test
        @Order(1)
        @DisplayName("Should detect simple change on field")
        void shouldDetectChangeOnField() {

            Map<String, Property> properties = Property
                    .computeProperties(ExampleEntity.class)
                    .stream()
                    .collect(Collectors.toMap(Property::getName, Function.identity()));

            ExampleEntity        entity     = ExampleEntity.create();
            State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
            Assertions.assertNotNull(stateProxy);

            Property activeProperty = properties.get("active");

            ExampleEntity proxy = stateProxy.getProxy();

            Assertions.assertFalse(stateProxy.isDirty());

            proxy.setActive(false);

            Map<Property, Object> state   = stateProxy.getOriginalState();
            Map<Property, Object> changes = stateProxy.getDifferentialState();

            Assertions.assertFalse(state.isEmpty());
            Assertions.assertFalse(changes.isEmpty());

            Assertions.assertTrue(state.containsKey(activeProperty));
            Assertions.assertTrue(changes.containsKey(activeProperty));

            Assertions.assertTrue((boolean) state.get(activeProperty));
            Assertions.assertFalse((boolean) changes.get(activeProperty));

            Assertions.assertFalse(entity.isActive());
            Assertions.assertFalse(proxy.isActive());

            Assertions.assertTrue(stateProxy.isDirty());
        }

        @Test
        @Order(2)
        @DisplayName("Should reset simple change on field")
        void shouldResetChangeOnField() {

            Map<String, Property> properties = Property
                    .computeProperties(ExampleEntity.class)
                    .stream()
                    .collect(Collectors.toMap(Property::getName, Function.identity()));

            ExampleEntity        entity     = ExampleEntity.create();
            State<ExampleEntity> stateProxy = Assertions.assertDoesNotThrow(() -> ClassProxyFactory.create(entity));
            Assertions.assertNotNull(stateProxy);

            Property activeProperty = properties.get("active");

            ExampleEntity proxy = stateProxy.getProxy();

            Assertions.assertFalse(stateProxy.isDirty());

            proxy.setActive(false);
            proxy.setActive(true);

            Map<Property, Object> state   = stateProxy.getOriginalState();
            Map<Property, Object> changes = stateProxy.getDifferentialState();

            Assertions.assertFalse(state.isEmpty());
            Assertions.assertTrue(changes.isEmpty());

            Assertions.assertTrue(state.containsKey(activeProperty));

            Assertions.assertTrue((boolean) state.get(activeProperty));

            Assertions.assertTrue(entity.isActive());
            Assertions.assertTrue(proxy.isActive());

            Assertions.assertFalse(stateProxy.isDirty());
        }


    }

}
