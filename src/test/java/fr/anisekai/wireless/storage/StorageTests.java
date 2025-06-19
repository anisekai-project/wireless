package fr.anisekai.wireless.storage;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.containers.stores.EntityDirectoryStore;
import fr.anisekai.wireless.api.storage.containers.stores.EntityStorageStore;
import fr.anisekai.wireless.api.storage.containers.stores.RawStorageStore;
import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.exceptions.*;
import fr.anisekai.wireless.api.storage.interfaces.Library;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;
import fr.anisekai.wireless.storage.data.ScopedEntityA;
import fr.anisekai.wireless.storage.data.ScopedEntityB;
import fr.anisekai.wireless.utils.FileUtils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

@DisplayName("Library Storage")
@Tags({@Tag("unit-test"), @Tag("library-storage")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class StorageTests {

    public static final Path TEST_LIBRARY_PATH = Path.of("test-data", "library");

    private static StorageStore randomRaw() {

        return new RawStorageStore(LibraryManager.getRandomName());
    }

    private static StorageStore randomDirStore(Class<? extends ScopedEntity> entityClass) {

        return new EntityDirectoryStore(LibraryManager.getRandomName(), entityClass);
    }

    private static StorageStore randomFileStore(Class<? extends ScopedEntity> entityClass) {

        return new EntityStorageStore(LibraryManager.getRandomName(), entityClass, "txt");
    }

    @Test
    @DisplayName("Library Creation | On File")
    public void testLibraryCreationOnFile() {

        Path root = Path.of("test-data", "video.mkv");

        LibraryInitializationException ex = Assertions.assertThrows(
                LibraryInitializationException.class,
                () -> new LibraryManager(root)
        );

        Assertions.assertTrue(ex.getMessage().contains("is a regular file"));
    }

    @Test
    @DisplayName("Store Registration | Name Clash")
    public void testStoreRegistrationNameClashes() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore store = randomRaw();

            Assertions.assertDoesNotThrow(() -> manager.registerStore(store, StorePolicy.PRIVATE));

            StorageRegistrationException ex = Assertions.assertThrows(
                    StorageRegistrationException.class,
                    () -> manager.registerStore(store, StorePolicy.PRIVATE)
            );

            Assertions.assertTrue(ex.getMessage().contains("already registered"));
        }
    }

    @Test
    @DisplayName("Store Registration | Raw Stores policies")
    public void testStoreRegistrationPolicyForRawStores() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore sPrivate   = randomRaw();
            StorageStore sOverwrite = randomRaw();
            StorageStore sFullSwap  = randomRaw();
            StorageStore sDiscard   = randomRaw();

            Assertions.assertDoesNotThrow(() -> manager.registerStore(sPrivate, StorePolicy.PRIVATE));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sDiscard, StorePolicy.DISCARD));

            StorageRegistrationException ex;

            ex = Assertions.assertThrows(
                    StorageRegistrationException.class,
                    () -> manager.registerStore(sOverwrite, StorePolicy.OVERWRITE)
            );

            Assertions.assertTrue(ex.getMessage().contains("policy"));

            ex = Assertions.assertThrows(
                    StorageRegistrationException.class,
                    () -> manager.registerStore(sFullSwap, StorePolicy.FULL_SWAP)
            );

            Assertions.assertTrue(ex.getMessage().contains("policy"));
        }
    }

    @Test
    @DisplayName("Store Registration | Entity Directory Stores policies")
    public void testStoreRegistrationPolicyForEntityDirectoryStores() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore sPrivate   = randomDirStore(ScopedEntityA.class);
            StorageStore sOverwrite = randomDirStore(ScopedEntityA.class);
            StorageStore sFullSwap  = randomDirStore(ScopedEntityA.class);
            StorageStore sDiscard   = randomDirStore(ScopedEntityA.class);

            Assertions.assertDoesNotThrow(() -> manager.registerStore(sPrivate, StorePolicy.PRIVATE));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sOverwrite, StorePolicy.OVERWRITE));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sFullSwap, StorePolicy.FULL_SWAP));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sDiscard, StorePolicy.DISCARD));
        }
    }

    @Test
    @DisplayName("Store Registration | Entity File Stores policies")
    public void testStoreRegistrationPolicyForEntityFileStores() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore sPrivate   = randomFileStore(ScopedEntityA.class);
            StorageStore sOverwrite = randomFileStore(ScopedEntityA.class);
            StorageStore sFullSwap  = randomFileStore(ScopedEntityA.class);
            StorageStore sDiscard   = randomFileStore(ScopedEntityA.class);

            Assertions.assertDoesNotThrow(() -> manager.registerStore(sPrivate, StorePolicy.PRIVATE));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sOverwrite, StorePolicy.OVERWRITE));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sFullSwap, StorePolicy.FULL_SWAP));
            Assertions.assertDoesNotThrow(() -> manager.registerStore(sDiscard, StorePolicy.DISCARD));
        }
    }

    @Test
    @DisplayName("Access Scope | Equality")
    public void testAccessScopeEquality() {

        StorageStore storeA = randomFileStore(ScopedEntityA.class);
        StorageStore storeB = randomFileStore(ScopedEntityB.class);

        ScopedEntity entityA1 = new ScopedEntityA("1");
        ScopedEntity entityA2 = new ScopedEntityA("2");
        ScopedEntity entityB1 = new ScopedEntityB("1");

        AccessScope scopeA1 = new AccessScope(storeA, entityA1);
        AccessScope scopeA2 = new AccessScope(storeA, entityA2);
        AccessScope scopeB1 = new AccessScope(storeB, entityB1);

        AccessScope scopeDupeA1 = new AccessScope(storeA, entityA1);
        AccessScope scopeDupeA2 = new AccessScope(storeA, entityA2);
        AccessScope scopeDupeB1 = new AccessScope(storeB, entityB1);

        Assertions.assertEquals(scopeA1, scopeDupeA1);
        Assertions.assertNotEquals(scopeA1, scopeDupeA2);
        Assertions.assertNotEquals(scopeA1, scopeDupeB1);

        Assertions.assertNotEquals(scopeA2, scopeDupeA1);
        Assertions.assertEquals(scopeA2, scopeDupeA2);
        Assertions.assertNotEquals(scopeA2, scopeDupeB1);

        Assertions.assertNotEquals(scopeB1, scopeDupeA1);
        Assertions.assertNotEquals(scopeB1, scopeDupeA2);
        Assertions.assertEquals(scopeB1, scopeDupeB1);
    }

    @Test
    @DisplayName("Access Scope | Creation")
    public void testAccessScopeCreation() {

        StorageStore rawStore    = randomRaw();
        StorageStore scopedStore = randomDirStore(ScopedEntityA.class);

        ScopedEntity entityA1             = new ScopedEntityA("1");
        ScopedEntity entityB1             = new ScopedEntityB("1");
        ScopedEntity veryBadAndMeanEntity = new ScopedEntityA("very bad and mean");

        ScopeDefinitionException ex;

        ex = Assertions.assertThrows(
                ScopeDefinitionException.class,
                () -> new AccessScope(rawStore, entityA1)
        );

        Assertions.assertTrue(ex.getMessage().contains("non-scoped"));

        ex = Assertions.assertThrows(
                ScopeDefinitionException.class,
                () -> new AccessScope(scopedStore, entityB1)
        );

        Assertions.assertTrue(ex.getMessage().contains("expecting type"));

        ex = Assertions.assertThrows(
                ScopeDefinitionException.class,
                () -> new AccessScope(scopedStore, veryBadAndMeanEntity)
        );

        Assertions.assertTrue(ex.getMessage().contains("match expected format"));

    }

    @Test
    @DisplayName("Library Stores | Store Out Of Bounds")
    public void testStoreOutOfBounds() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore outOfBounds = new RawStorageStore("../out-of-bounds");

            StorageRegistrationException ex = Assertions.assertThrows(
                    StorageRegistrationException.class,
                    () -> manager.registerStore(outOfBounds, StorePolicy.PRIVATE)
            );

            StorageOutOfBoundException soob = Assertions.assertInstanceOf(StorageOutOfBoundException.class, ex.getCause());
            Assertions.assertTrue(soob.getMessage().contains("out-of-bound"));
        }
    }

    @Test
    @DisplayName("Library Stores | Store directory creation failure")
    public void testStoreCreationOnFile() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageStore store = randomRaw();

            File lock = new File(TEST_LIBRARY_PATH.toFile(), store.name());
            Assertions.assertDoesNotThrow(lock::createNewFile, "fuck, this shit is not even part of the test");

            StorageRegistrationException sre = Assertions.assertThrows(
                    StorageRegistrationException.class,
                    () -> manager.registerStore(store, StorePolicy.PRIVATE)
            );

            Assertions.assertTrue(sre.getMessage().contains("could not be registered"));
            StorageAccessException sae = Assertions.assertInstanceOf(StorageAccessException.class, sre.getCause());
            Assertions.assertTrue(sae.getMessage().contains("is a regular file"));
        }
    }

    @Test
    @DisplayName("Isolation Context | Create with no scope")
    public void testIsolationCreationNoScope() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation());

            try (context) {
                Path contextPath = TEST_LIBRARY_PATH.resolve("isolation").resolve(context.name());
                Assertions.assertTrue(Files.exists(contextPath));
            }
        }
    }

    @Test
    @DisplayName("Isolation Context | Create with a scope")
    public void testIsolationCreationValidScope() throws Exception {

        ScopedEntityA entity = new ScopedEntityA("1");
        StorageStore  store  = randomFileStore(ScopedEntityA.class);
        AccessScope   scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);
            StorageIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scope));

            try (context) {
                Path contextPath = TEST_LIBRARY_PATH.resolve("isolation").resolve(context.name());
                Assertions.assertTrue(Files.exists(contextPath));
            }
        }
    }

    @Test
    @DisplayName("Isolation Context | Create with a scope in use")
    public void testIsolationCreationScopeClash() throws Exception {

        ScopedEntityA entity = new ScopedEntityA("1");
        StorageStore  store  = randomFileStore(ScopedEntityA.class);
        AccessScope   scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);
            StorageIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scope));

            try (context) {
                ScopeGrantException ex = Assertions.assertThrows(ScopeGrantException.class, () -> manager.createIsolation(scope));
                Assertions.assertTrue(ex.getMessage().contains("is already claimed"));
            }
        }
    }

    @Test
    @DisplayName("Isolation Context | Request used scope")
    public void testIsolationRequestUsedScope() throws Exception {

        StorageStore  store   = randomFileStore(ScopedEntityA.class);
        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");
        AccessScope   scopeA  = new AccessScope(store, entityA);
        AccessScope   scopeB  = new AccessScope(store, entityB);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            StorageIsolationContext contextA = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA));
            StorageIsolationContext contextB = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeB));

            ScopeGrantException ex;

            ex = Assertions.assertThrows(ScopeGrantException.class, () -> contextA.requestScope(scopeB));
            Assertions.assertTrue(ex.getMessage().contains("is already claimed"));

            ex = Assertions.assertThrows(ScopeGrantException.class, () -> contextB.requestScope(scopeA));
            Assertions.assertTrue(ex.getMessage().contains("is already claimed"));

            contextA.close();
            contextB.close();
        }
    }

    @Test
    @DisplayName("Isolation Context | Request unused scope")
    public void testIsolationRequestUnusedScope() throws Exception {

        StorageStore  store   = randomFileStore(ScopedEntityA.class);
        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");
        AccessScope   scopeA  = new AccessScope(store, entityA);
        AccessScope   scopeB  = new AccessScope(store, entityB);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            StorageIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA));

            try (context) {
                Assertions.assertDoesNotThrow(() -> context.requestScope(scopeB));
            }
        }
    }

    @Test
    @DisplayName("Isolation Context | Request freed scope")
    public void testIsolationRequestFreedScope() throws Exception {

        StorageStore  store   = randomFileStore(ScopedEntityA.class);
        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");
        AccessScope   scopeA  = new AccessScope(store, entityA);
        AccessScope   scopeB  = new AccessScope(store, entityB);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            StorageIsolationContext contextA = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA));
            StorageIsolationContext contextB = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeB));

            ScopeGrantException ex = Assertions.assertThrows(
                    ScopeGrantException.class,
                    () -> contextA.requestScope(scopeB)
            );

            Assertions.assertTrue(ex.getMessage().contains("is already claimed"));
            contextB.close();

            Assertions.assertDoesNotThrow(() -> contextA.requestScope(scopeB));
        }
    }

    @Test
    @DisplayName("Isolation Context | Create on unregistered store")
    public void testIsolationCreationStoreNotSupported() throws Exception {

        StorageStore  store  = randomFileStore(ScopedEntityA.class);
        ScopedEntityA entity = new ScopedEntityA("1");
        AccessScope   scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {

            StorageAccessException ex = Assertions.assertThrows(
                    StorageAccessException.class,
                    () -> manager.createIsolation(scope)
            );
            Assertions.assertTrue(ex.getMessage().contains("is not registered on the library"));
        }
    }

    @Test
    @DisplayName("Isolation Context | Forbidden use after commit/discard")
    public void testIsolationUseAfterCommit() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation());
            context.commit();

            ContextUnavailableException ex;

            ex = Assertions.assertThrows(ContextUnavailableException.class, context::commit);
            Assertions.assertTrue(ex.getMessage().contains("is already committed"));

            ex = Assertions.assertThrows(ContextUnavailableException.class, () -> context.requestTemporaryFile("txt"));
            Assertions.assertTrue(ex.getMessage().contains("is already committed"));

            context.close();

            ex = Assertions.assertThrows(ContextUnavailableException.class, context::commit);
            Assertions.assertTrue(ex.getMessage().contains("has already been discarded"));

            ex = Assertions.assertThrows(ContextUnavailableException.class, () -> context.requestTemporaryFile("txt"));
            Assertions.assertTrue(ex.getMessage().contains("has already been discarded"));
        }
    }

    @Test
    @DisplayName("Isolation Writing | Write to a temporary file")
    public void testIsolationWritingToTemporaryFile() throws Exception {

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            StorageIsolationContext context = manager.createIsolation(Collections.emptySet());

            try (context) {
                Path temporary = Assertions.assertDoesNotThrow(() -> context.requestTemporaryFile("txt"));

                Assertions.assertDoesNotThrow(() -> Files.writeString(
                        temporary,
                        "Unit Test",
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                ));

                context.commit();
            }
        }
    }

    @Test
    @DisplayName("Isolation Resolution | EntityFileStore")
    public void testIsolationResolutionOnEntityFileStore() throws Exception {

        StorageStore store  = randomFileStore(ScopedEntityA.class);
        ScopedEntity entity = new ScopedEntityA("1");
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Assertions.assertThrows(StorageForbiddenException.class, () -> context.resolveScope(scope, "unit.txt"));
                Assertions.assertDoesNotThrow(() -> context.resolveScope(scope));
            }
        }
    }

    @Test
    @DisplayName("Isolation Resolution | EntityDirectoryStore")
    public void testIsolationResolutionOnEntityDirectoryStore() throws Exception {

        StorageStore store  = randomDirStore(ScopedEntityA.class);
        ScopedEntity entity = new ScopedEntityA("1");
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Assertions.assertDoesNotThrow(() -> context.resolveScope(scope));
                Assertions.assertDoesNotThrow(() -> context.resolveScope(scope, "unit.txt"));
            }
        }
    }

    @Test
    @DisplayName("Isolation Writing | EntityFileStore")
    public void testIsolationWritingEntityFileStore() throws Exception {

        StorageStore store   = randomFileStore(ScopedEntityA.class);
        ScopedEntity entity  = new ScopedEntityA("1");
        AccessScope  scope   = new AccessScope(store, entity);
        String       content = "UnitTest";
        byte[]       bytes   = content.getBytes(StandardCharsets.UTF_8);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope);

                InputStream  is = new ByteArrayInputStream(bytes);
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Path output = manager.resolveScope(scope);
            Assertions.assertTrue(Files.isRegularFile(output));
            String finalContent = Files.readString(output, StandardCharsets.UTF_8);
            Assertions.assertEquals(content, finalContent);
        }
    }

    @Test
    @DisplayName("Isolation Writing | EntityDirectoryStore")
    public void testIsolationWritingEntityDirectoryStore() throws Exception {

        StorageStore store   = randomDirStore(ScopedEntityA.class);
        ScopedEntity entity  = new ScopedEntityA("1");
        AccessScope  scope   = new AccessScope(store, entity);
        String       content = "UnitTest";
        byte[]       bytes   = content.getBytes(StandardCharsets.UTF_8);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope, "unit.txt");

                InputStream  is = new ByteArrayInputStream(bytes);
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Path output = manager.resolveScope(scope, "unit.txt");
            Assertions.assertTrue(Files.isRegularFile(output));
            String finalContent = Files.readString(output, StandardCharsets.UTF_8);
            Assertions.assertEquals(content, finalContent);
        }
    }


    @Test
    @DisplayName("Store Policy | Directory Overwrite")
    public void testStorePolicyDirectoryOverwrite() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomDirStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            String startingContent = "unit-test-start";
            String endingContent   = "unit-test-end";
            String staticContent   = "unit-test-static";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path staticPath   = manager.resolveScope(scope, "static.txt");
            Path replacedPath = manager.resolveScope(scope, "replaced.txt");
            Files.writeString(staticPath, staticContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Files.writeString(replacedPath, startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope, "replaced.txt");

                InputStream  is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8));
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Assertions.assertTrue(Files.isRegularFile(staticPath));
            Assertions.assertTrue(Files.isRegularFile(replacedPath));

            String finalStaticContent   = Files.readString(staticPath);
            String finalReplacedContent = Files.readString(replacedPath);

            Assertions.assertEquals(staticContent, finalStaticContent);
            Assertions.assertEquals(endingContent, finalReplacedContent);
        }
    }

    @Test
    @DisplayName("Store Policy | Directory Full Swap")
    public void testStorePolicyDirectoryFullSwap() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomDirStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.FULL_SWAP);

            String startingContent = "unit-test-start";
            String endingContent   = "unit-test-end";
            String staticContent   = "unit-test-static";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path staticPath   = manager.resolveScope(scope, "static.txt");
            Path replacedPath = manager.resolveScope(scope, "replaced.txt");
            Files.writeString(staticPath, staticContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Files.writeString(replacedPath, startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope, "replaced.txt");

                InputStream  is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8));
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Assertions.assertFalse(Files.isRegularFile(staticPath));
            Assertions.assertTrue(Files.isRegularFile(replacedPath));

            String finalReplacedContent = Files.readString(replacedPath);

            Assertions.assertEquals(endingContent, finalReplacedContent);
        }
    }

    @Test
    @DisplayName("Store Policy | File Overwrite (Content)")
    public void testStorePolicyFileOverwriteContent() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomFileStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            String startingContent = "unit-test-start";
            String endingContent   = "unit-test-end";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path path = manager.resolveScope(scope);
            Files.writeString(path, startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope);

                InputStream  is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8));
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Assertions.assertTrue(Files.isRegularFile(path));

            String finalReplacedContent = Files.readString(path);

            Assertions.assertEquals(endingContent, finalReplacedContent);
        }
    }

    @Test
    @DisplayName("Store Policy | File Overwrite (No Content)")
    public void testStorePolicyFileOverwriteNoContent() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomFileStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.OVERWRITE);

            String content = "unit-test";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path path = manager.resolveScope(scope);
            Files.writeString(path, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                context.commit();
            }

            Assertions.assertTrue(Files.isRegularFile(path));

            String finalContent = Files.readString(path);

            Assertions.assertEquals(content, finalContent);
        }
    }

    @Test
    @DisplayName("Store Policy | File Full Swap (Content)")
    public void testStorePolicyFileFullSwapContent() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomFileStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.FULL_SWAP);

            String startingContent = "unit-test-start";
            String endingContent   = "unit-test-end";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path path = manager.resolveScope(scope);
            Files.writeString(path, startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                Path output = context.resolveScope(scope);

                InputStream  is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8));
                OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

                try (is; os) {
                    is.transferTo(os);
                }

                context.commit();
            }

            Assertions.assertTrue(Files.isRegularFile(path));

            String finalReplacedContent = Files.readString(path);

            Assertions.assertEquals(endingContent, finalReplacedContent);
        }
    }

    @Test
    @DisplayName("Store Policy | File Full Swap (No Content)")
    public void testStorePolicyFileFullSwapNoContent() throws Exception {

        ScopedEntity entity = new ScopedEntityA("1");
        StorageStore store  = randomFileStore(ScopedEntityA.class);
        AccessScope  scope  = new AccessScope(store, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(store, StorePolicy.FULL_SWAP);

            String content = "unit-test";

            // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
            Path path = manager.resolveScope(scope);
            Files.writeString(path, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            try (StorageIsolationContext context = manager.createIsolation(scope)) {
                context.commit();
            }

            Assertions.assertFalse(Files.isRegularFile(path));
        }
    }

    @Test
    @DisplayName("File Access Boundaries")
    public void testFileAccessBoundaries() throws Exception {

        ScopedEntity entity      = new ScopedEntityA("1");
        String       oobFilename = "../escape-path.txt";
        String       filename    = "normal.txt";
        String       veryMeanOob = ".txt/../../escape-path.txt";

        StorageStore scopedStore = randomDirStore(ScopedEntityA.class);
        StorageStore rawStore    = randomRaw();

        AccessScope scope = new AccessScope(scopedStore, entity);

        try (Library manager = new LibraryManager(TEST_LIBRARY_PATH)) {
            manager.registerStore(scopedStore, StorePolicy.OVERWRITE);
            manager.registerStore(rawStore, StorePolicy.DISCARD);

            Path path, storeRoot;

            // Test library boundaries
            storeRoot = Assertions.assertDoesNotThrow(() -> manager.resolveDirectory(rawStore));
            path      = Assertions.assertDoesNotThrow(() -> manager.resolveFile(rawStore, filename));
            Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, path));

            storeRoot = Assertions.assertDoesNotThrow(() -> manager.resolveDirectory(scopedStore, entity));
            path      = Assertions.assertDoesNotThrow(() -> manager.resolveFile(scopedStore, entity, filename));
            Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, path));

            Assertions.assertThrows(StorageOutOfBoundException.class, () -> manager.resolveFile(rawStore, oobFilename));
            Assertions.assertThrows(
                    StorageOutOfBoundException.class,
                    () -> manager.resolveFile(scopedStore, entity, oobFilename)
            );
            Assertions.assertThrows(StorageOutOfBoundException.class, () -> manager.resolveFile(rawStore, veryMeanOob));
            Assertions.assertThrows(
                    StorageOutOfBoundException.class,
                    () -> manager.resolveFile(scopedStore, entity, veryMeanOob)
            );


            // Test isolation boundaries
            try (StorageIsolationContext context = manager.createIsolation(scope)) {

                storeRoot = Assertions.assertDoesNotThrow(() -> context.resolveScope(scope));
                path      = Assertions.assertDoesNotThrow(() -> context.resolveScope(scope, filename));
                Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, path));

                Assertions.assertThrows(
                        StorageOutOfBoundException.class,
                        () -> context.resolveScope(scope, oobFilename)
                );
                Assertions.assertThrows(StorageOutOfBoundException.class, () -> context.requestTemporaryFile(veryMeanOob));
            }
        }
    }


    @AfterEach
    public void cleanup() throws IOException {

        FileUtils.delete(TEST_LIBRARY_PATH);
    }

}
