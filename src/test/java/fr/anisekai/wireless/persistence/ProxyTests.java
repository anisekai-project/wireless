package fr.anisekai.wireless.persistence;

import fr.anisekai.wireless.api.persistence.EventProxyImpl;
import fr.anisekai.wireless.persistence.data.ITestEntity;
import fr.anisekai.wireless.persistence.data.TestEntity;
import fr.anisekai.wireless.api.persistence.interfaces.EntityUpdatedEvent;
import fr.anisekai.wireless.api.persistence.interfaces.EventProxy;
import org.junit.jupiter.api.*;

import java.util.List;

@DisplayName("Proxy")
@Tags({@Tag("unit-test"), @Tag("entity-proxy")})
public class ProxyTests {

    private static final String TEST_VAL_STR_1 = "unit test";
    private static final String TEST_VAL_STR_2 = "lorem ipsum";

    private static EventProxy<ITestEntity, TestEntity> createProxy(TestEntity entity) {

        return new EventProxyImpl<>(entity) {
            @Override
            public EntityUpdatedEvent<TestEntity, ?> createEvent(Class<? extends EntityUpdatedEvent<?, ?>> eventType, TestEntity entity, Object oldValue, Object newValue) throws Exception {

                return (EntityUpdatedEvent<TestEntity, ?>)
                        eventType.getConstructor(Object.class, entity.getClass(), String.class, String.class)
                                 .newInstance(this, entity, oldValue, newValue);
            }
        };
    }

    @Test
    @DisplayName("Persistence | Update entity | null -> something")
    public void testEventProxyNullToSomething() {

        TestEntity entity = new TestEntity();

        EventProxy<ITestEntity, TestEntity> eventProxy = Assertions.assertDoesNotThrow(() -> createProxy(entity));
        ITestEntity                         proxy      = eventProxy.startProxy();

        proxy.setName(TEST_VAL_STR_1);

        List<EntityUpdatedEvent<TestEntity, ?>> events = eventProxy.getEvents();

        Assertions.assertEquals(1, events.size(), "No event or too much events have been generated.");
        EntityUpdatedEvent<TestEntity, ?> event = events.getFirst();

        Assertions.assertNull(event.getOldValue(), "Wrong old value in event");
        Assertions.assertEquals(TEST_VAL_STR_1, event.getNewValue(), "Wrong new value in event");
        Assertions.assertEquals(TEST_VAL_STR_1, proxy.getName(), "Proxy getter not redirected");
        Assertions.assertEquals(TEST_VAL_STR_1, entity.getName(), "Proxy did not edit entity");
    }

    @Test
    @DisplayName("Persistence | Update entity | something -> null")
    public void testEventProxySomethingToNull() {

        TestEntity entity = new TestEntity();
        entity.setName(TEST_VAL_STR_1);

        EventProxy<ITestEntity, TestEntity> eventProxy = Assertions.assertDoesNotThrow(() -> createProxy(entity));
        ITestEntity                         proxy      = eventProxy.startProxy();

        proxy.setName(null);

        List<EntityUpdatedEvent<TestEntity, ?>> events = eventProxy.getEvents();

        Assertions.assertEquals(1, events.size(), "No event or too much events have been generated.");
        EntityUpdatedEvent<TestEntity, ?> event = events.getFirst();

        Assertions.assertEquals(TEST_VAL_STR_1, event.getOldValue(), "Wrong old value in event");
        Assertions.assertNull(event.getNewValue(), "Wrong new value in event");
        Assertions.assertNull(proxy.getName(), "Proxy getter not redirected");
        Assertions.assertNull(entity.getName(), "Proxy did not edit entity");
    }

    @Test
    @DisplayName("Persistence | Update entity | something -> something")
    public void testEventProxySomethingToSomething() {

        TestEntity entity = new TestEntity();
        entity.setName(TEST_VAL_STR_1);

        EventProxy<ITestEntity, TestEntity> eventProxy = Assertions.assertDoesNotThrow(() -> createProxy(entity));
        ITestEntity                         proxy      = eventProxy.startProxy();

        proxy.setName(TEST_VAL_STR_2);

        List<EntityUpdatedEvent<TestEntity, ?>> events = eventProxy.getEvents();

        Assertions.assertEquals(1, events.size(), "No event or too much events have been generated.");
        EntityUpdatedEvent<TestEntity, ?> event = events.getFirst();

        Assertions.assertEquals(TEST_VAL_STR_1, event.getOldValue(), "Wrong old value in event");
        Assertions.assertEquals(TEST_VAL_STR_2, event.getNewValue(), "Wrong new value in event");
        Assertions.assertEquals(TEST_VAL_STR_2, proxy.getName(), "Proxy getter not redirected");
        Assertions.assertEquals(TEST_VAL_STR_2, entity.getName(), "Proxy did not edit entity");
    }

    @Test
    @DisplayName("Persistence | Update entity | no changes")
    public void testEventProxyNoChanges() {

        TestEntity entity = new TestEntity();
        entity.setName(TEST_VAL_STR_1);

        EventProxy<ITestEntity, TestEntity> eventProxy = Assertions.assertDoesNotThrow(() -> createProxy(entity));
        ITestEntity                         proxy      = eventProxy.startProxy();

        proxy.setName(TEST_VAL_STR_1);

        Assertions.assertTrue(eventProxy.getEvents().isEmpty(), "No event should have been generated.");
    }

    @Test
    @DisplayName("Persistence | Update entity | rollback")
    public void testEventProxyRollback() {

        TestEntity entity = new TestEntity();
        entity.setName(TEST_VAL_STR_1);

        EventProxy<ITestEntity, TestEntity> eventProxy = Assertions.assertDoesNotThrow(() -> createProxy(entity));
        ITestEntity                         proxy      = eventProxy.startProxy();

        proxy.setName(TEST_VAL_STR_2);
        proxy.setName(TEST_VAL_STR_1);

        Assertions.assertTrue(eventProxy.getEvents().isEmpty(), "No event should have been generated.");
    }

}
