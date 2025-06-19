package fr.anisekai.wireless.storage;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.containers.stores.EntityDirectoryStore;
import fr.anisekai.wireless.api.storage.containers.stores.EntityFileStore;
import fr.anisekai.wireless.api.storage.containers.stores.RawFileStore;
import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.exceptions.*;
import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.storage.data.ScopedEntityA;
import fr.anisekai.wireless.storage.data.ScopedEntityB;
import fr.anisekai.wireless.utils.FileUtils;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

@DisplayName("Library Storage")
@Tags({@Tag("unit-test"), @Tag("library-storage")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class StorageTests {

    private static FileStore randomRaw() {

        return new RawFileStore(LibraryManager.getRandomName());
    }

    private static FileStore randomDirStore(Class<? extends ScopedEntity> entityClass) {

        return new EntityDirectoryStore(LibraryManager.getRandomName(), entityClass);
    }

    private static FileStore randomFileStore(Class<? extends ScopedEntity> entityClass) {

        return new EntityFileStore(LibraryManager.getRandomName(), entityClass, "txt");
    }

    @Test
    @DisplayName("Library Creation | On File")
    public void testLibraryCreationOnFile() {

        File testData = new File("test-data");
        File library  = new File(testData, "video.mkv");

        IllegalStateException ex = Assertions.assertThrows(
                IllegalStateException.class,
                () -> new LibraryManager(library)
        );

        Assertions.assertEquals("Failure while initializing library.", ex.getMessage());
    }

    @Test
    @DisplayName("Store Registration | Name Clash")
    public void testStoreRegistrationNameClashes() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomRaw();

        Assertions.assertDoesNotThrow(() -> manager.register(store, StorePolicy.PRIVATE));

        StoreRegistrationException ex = Assertions.assertThrows(
                StoreRegistrationException.class,
                () -> manager.register(store, StorePolicy.PRIVATE)
        );

        Assertions.assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("Store Registration | Raw Stores policies")
    public void testStoreRegistrationPolicyForRawStores() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore sPrivate   = randomRaw();
        FileStore sOverwrite = randomRaw();
        FileStore sFullSwap  = randomRaw();
        FileStore sDiscard   = randomRaw();

        Assertions.assertDoesNotThrow(() -> manager.register(sPrivate, StorePolicy.PRIVATE));
        Assertions.assertDoesNotThrow(() -> manager.register(sDiscard, StorePolicy.DISCARD));

        StoreRegistrationException ex;

        ex = Assertions.assertThrows(
                StoreRegistrationException.class,
                () -> manager.register(sOverwrite, StorePolicy.OVERWRITE)
        );

        Assertions.assertTrue(ex.getMessage().contains("policy"));

        ex = Assertions.assertThrows(
                StoreRegistrationException.class,
                () -> manager.register(sFullSwap, StorePolicy.FULL_SWAP)
        );

        Assertions.assertTrue(ex.getMessage().contains("policy"));
    }

    @Test
    @DisplayName("Store Registration | Entity Directory Stores policies")
    public void testStoreRegistrationPolicyForEntityDirectoryStores() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore sPrivate   = randomDirStore(ScopedEntityA.class);
        FileStore sOverwrite = randomDirStore(ScopedEntityA.class);
        FileStore sFullSwap  = randomDirStore(ScopedEntityA.class);
        FileStore sDiscard   = randomDirStore(ScopedEntityA.class);

        Assertions.assertDoesNotThrow(() -> manager.register(sPrivate, StorePolicy.PRIVATE));
        Assertions.assertDoesNotThrow(() -> manager.register(sOverwrite, StorePolicy.OVERWRITE));
        Assertions.assertDoesNotThrow(() -> manager.register(sFullSwap, StorePolicy.FULL_SWAP));
        Assertions.assertDoesNotThrow(() -> manager.register(sDiscard, StorePolicy.DISCARD));
    }

    @Test
    @DisplayName("Store Registration | Entity File Stores policies")
    public void testStoreRegistrationPolicyForEntityFileStores() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore sPrivate   = randomFileStore(ScopedEntityA.class);
        FileStore sOverwrite = randomFileStore(ScopedEntityA.class);
        FileStore sFullSwap  = randomFileStore(ScopedEntityA.class);
        FileStore sDiscard   = randomFileStore(ScopedEntityA.class);

        Assertions.assertDoesNotThrow(() -> manager.register(sPrivate, StorePolicy.PRIVATE));
        Assertions.assertDoesNotThrow(() -> manager.register(sOverwrite, StorePolicy.OVERWRITE));
        Assertions.assertDoesNotThrow(() -> manager.register(sFullSwap, StorePolicy.FULL_SWAP));
        Assertions.assertDoesNotThrow(() -> manager.register(sDiscard, StorePolicy.DISCARD));
    }

    @Test
    @DisplayName("Access Scope | Equality")
    public void testAccessScopeEquality() {

        FileStore storeA = randomFileStore(ScopedEntityA.class);
        FileStore storeB = randomFileStore(ScopedEntityB.class);

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

        FileStore rawStore    = randomRaw();
        FileStore scopedStore = randomDirStore(ScopedEntityA.class);

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
    public void testStoreOutOfBounds() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore outOfBounds = new RawFileStore("../out-of-bounds");

        StoreBreakoutException ex = Assertions.assertThrows(
                StoreBreakoutException.class,
                () -> manager.register(outOfBounds, StorePolicy.PRIVATE)
        );

        Assertions.assertTrue(ex.getMessage().contains("out-of-bound"));
    }

    @Test
    @DisplayName("Library Stores | Store directory creation failure")
    public void testStoreCreationOnFile() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomRaw();

        File lock = new File(library, store.name());
        Assertions.assertDoesNotThrow(lock::createNewFile, "fuck, this shit is not even part of the test");

        StoreRegistrationException ex = Assertions.assertThrows(
                StoreRegistrationException.class,
                () -> manager.register(store, StorePolicy.PRIVATE)
        );

        Assertions.assertTrue(ex.getMessage().contains("Failure while registering"));
    }

    @Test
    @DisplayName("Isolation Context | Create with no scope")
    public void testIsolationCreationNoScope() {

        File           testData  = new File("test-data");
        File           library   = new File(testData, "library");
        File           isolation = new File(library, "isolation");
        LibraryManager manager   = new LibraryManager(library);

        FileIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation());

        try (context) {
            File contextRoot = new File(isolation, context.name());
            Assertions.assertTrue(isolation.exists());
            Assertions.assertTrue(contextRoot.exists());
        }
    }

    @Test
    @DisplayName("Isolation Context | Create with a scope")
    public void testIsolationCreationValidScope() {

        ScopedEntityA  entity    = new ScopedEntityA("1");
        File           testData  = new File("test-data");
        File           library   = new File(testData, "library");
        File           isolation = new File(library, "isolation");
        LibraryManager manager   = new LibraryManager(library);

        FileStore store = randomFileStore(ScopedEntityA.class);
        manager.register(store, StorePolicy.OVERWRITE);

        AccessScope          scope   = new AccessScope(store, entity);
        FileIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scope));

        try (context) {
            File contextRoot = new File(isolation, context.name());
            Assertions.assertTrue(isolation.exists());
            Assertions.assertTrue(contextRoot.exists());
        }
    }

    @Test
    @DisplayName("Isolation Context | Create with a scope in use")
    public void testIsolationCreationScopeClash() {

        ScopedEntityA  entity   = new ScopedEntityA("1");
        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomFileStore(ScopedEntityA.class);
        manager.register(store, StorePolicy.OVERWRITE);

        AccessScope          scope   = new AccessScope(store, entity);
        FileIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scope));

        try (context) {
            ScopeGrantException ex = Assertions.assertThrows(ScopeGrantException.class, () -> manager.createIsolation(scope));
            Assertions.assertTrue(ex.getMessage().contains("already claimed"));
        }
    }

    @Test
    @DisplayName("Isolation Context | Request used scope")
    public void testIsolationRequestUsedScope() {

        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomFileStore(ScopedEntityA.class);
        manager.register(store, StorePolicy.OVERWRITE);

        AccessScope scopeA = new AccessScope(store, entityA);
        AccessScope scopeB = new AccessScope(store, entityB);

        FileIsolationContext contextA = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA));
        FileIsolationContext contextB = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeB));

        ScopeGrantException ex;

        ex = Assertions.assertThrows(ScopeGrantException.class, () -> contextA.requestScope(scopeB));
        Assertions.assertTrue(ex.getMessage().contains("already claimed"));

        ex = Assertions.assertThrows(ScopeGrantException.class, () -> contextB.requestScope(scopeA));
        Assertions.assertTrue(ex.getMessage().contains("already claimed"));

        contextA.close();
        contextB.close();
    }

    @Test
    @DisplayName("Isolation Context | Request unused scope")
    public void testIsolationRequestUnusedScope() {

        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomFileStore(ScopedEntityA.class);
        manager.register(store, StorePolicy.OVERWRITE);

        AccessScope scopeA = new AccessScope(store, entityA);
        AccessScope scopeB = new AccessScope(store, entityB);

        FileIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA));

        try (context) {
            Assertions.assertDoesNotThrow(() -> context.requestScope(scopeB));
        }
    }

    @Test
    @DisplayName("Isolation Context | Request freed scope")
    public void testIsolationRequestFreedScope() {

        ScopedEntityA entityA = new ScopedEntityA("A");
        ScopedEntityA entityB = new ScopedEntityA("B");

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore store = randomFileStore(ScopedEntityA.class);
        manager.register(store, StorePolicy.OVERWRITE);

        AccessScope scopeA = new AccessScope(store, entityA);
        AccessScope scopeB = new AccessScope(store, entityB);

        try (FileIsolationContext outsideContext = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeA))) {

            try (FileIsolationContext insideContext = Assertions.assertDoesNotThrow(() -> manager.createIsolation(scopeB))) {

                ScopeGrantException ex = Assertions.assertThrows(
                        ScopeGrantException.class,
                        () -> outsideContext.requestScope(scopeB)
                );
                Assertions.assertTrue(ex.getMessage().contains("already claimed"));

                insideContext.commit();
            }

            Assertions.assertDoesNotThrow(() -> outsideContext.requestScope(scopeB));
        }
    }

    @Test
    @DisplayName("Isolation Context | Create on unregistered store")
    public void testIsolationCreationStoreNotSupported() {

        ScopedEntityA  entity   = new ScopedEntityA("1");
        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileStore   store = randomFileStore(ScopedEntityA.class);
        AccessScope scope = new AccessScope(store, entity);

        StoreAccessException ex = Assertions.assertThrows(StoreAccessException.class, () -> manager.createIsolation(scope));

        Assertions.assertTrue(ex.getMessage().contains("does not support"));
    }

    @Test
    @DisplayName("Isolation Context | Forbidden use after commit/discard")
    public void testIsolationUseAfterCommit() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        FileIsolationContext context = Assertions.assertDoesNotThrow(() -> manager.createIsolation());
        context.commit();

        ContextUnavailableException ex;

        ex = Assertions.assertThrows(ContextUnavailableException.class, context::commit);
        Assertions.assertTrue(ex.getMessage().contains("already committed"));

        ex = Assertions.assertThrows(ContextUnavailableException.class, () -> context.requestTemporaryFile("txt"));
        Assertions.assertTrue(ex.getMessage().contains("already committed"));

        context.close();

        ex = Assertions.assertThrows(ContextUnavailableException.class, context::commit);
        Assertions.assertTrue(ex.getMessage().contains("already discarded"));

        ex = Assertions.assertThrows(ContextUnavailableException.class, () -> context.requestTemporaryFile("txt"));
        Assertions.assertTrue(ex.getMessage().contains("already discarded"));
    }

    @Test
    @DisplayName("Isolation Writing | Write to a temporary file")
    public void testIsolationWritingToTemporaryFile() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        try (FileIsolationContext context = manager.createIsolation(Collections.emptySet())) {
            File temporary = Assertions.assertDoesNotThrow(() -> context.requestTemporaryFile("txt"));

            Assertions.assertDoesNotThrow(() -> Files.writeString(
                    temporary.toPath(),
                    "Unit Test",
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            ));

            context.commit();
        }
    }

    @Test
    @DisplayName("Isolation Writing | EntityFileStore | Named File should fail")
    public void testIsolationWritingEntityFileStoreNamedFile() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity  = new ScopedEntityA("1");
        FileStore        store   = randomFileStore(ScopedEntityA.class);
        AccessScope      scope   = new AccessScope(store, entity);
        Set<AccessScope> scopes  = Set.of(scope);
        String           content = "UnitTest";

        manager.register(store, StorePolicy.OVERWRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            try (InputStream is = new ByteArrayInputStream(bytes)) {
                Assertions.assertThrows(StoreAccessException.class, () -> context.store(scope, "unit.txt", is));
            }
        }
    }

    @Test
    @DisplayName("Isolation Writing | EntityFileStore | Write")
    public void testIsolationWritingEntityFileStore() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity  = new ScopedEntityA("1");
        FileStore        store   = randomFileStore(ScopedEntityA.class);
        AccessScope      scope   = new AccessScope(store, entity);
        Set<AccessScope> scopes  = Set.of(scope);
        String           content = "UnitTest";

        manager.register(store, StorePolicy.OVERWRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            try (InputStream is = new ByteArrayInputStream(bytes)) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, is));
            }

            context.commit();
        }

        File file = manager.getStoreFile(store, entity);
        Assertions.assertTrue(file.exists());
        String finalContent = Files.readString(file.toPath());

        Assertions.assertEquals(content, finalContent);
    }

    @Test
    @DisplayName("Isolation Writing | EntityDirectoryStore | Write")
    public void testIsolationWritingEntityDirectoryStore() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity  = new ScopedEntityA("1");
        FileStore        store   = randomDirStore(ScopedEntityA.class);
        AccessScope      scope   = new AccessScope(store, entity);
        Set<AccessScope> scopes  = Set.of(scope);
        String           content = "UnitTest";

        manager.register(store, StorePolicy.OVERWRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            try (InputStream is = new ByteArrayInputStream(bytes)) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, "unit.txt", is));
            }

            context.commit();
        }

        File file = manager.getStoreFile(store, entity, "unit.txt");
        Assertions.assertTrue(file.exists());
        String finalContent = Files.readString(file.toPath());

        Assertions.assertEquals(content, finalContent);
    }

    @Test
    @DisplayName("Isolation Writing | EntityDirectoryStore | Unnamed File should fail")
    public void testIsolationWritingEntityDirectoryStoreUnnamedFile() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity  = new ScopedEntityA("1");
        FileStore        store   = randomDirStore(ScopedEntityA.class);
        AccessScope      scope   = new AccessScope(store, entity);
        Set<AccessScope> scopes  = Set.of(scope);
        String           content = "UnitTest";

        manager.register(store, StorePolicy.OVERWRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            try (InputStream is = new ByteArrayInputStream(bytes)) {
                Assertions.assertThrows(StoreAccessException.class, () -> context.store(scope, is));
            }
        }
    }

    @Test
    @DisplayName("Store Policy | Directory Overwrite")
    public void testStorePolicyDirectoryOverwrite() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomDirStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.OVERWRITE);

        String startingContent = "unit-test-start";
        String endingContent   = "unit-test-end";
        String staticContent   = "unit-test-static";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File staticFile   = manager.getStoreFile(store, entity, "static.txt");
        File replacedFile = manager.getStoreFile(store, entity, "replaced.txt");
        Files.writeString(staticFile.toPath(), staticContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        Files.writeString(replacedFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            try (InputStream is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8))) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, "replaced.txt", is));
            }

            context.commit();
        }

        Assertions.assertTrue(staticFile.exists());
        Assertions.assertTrue(replacedFile.exists());

        String finalStaticContent   = Files.readString(staticFile.toPath());
        String finalReplacedContent = Files.readString(replacedFile.toPath());

        Assertions.assertEquals(staticContent, finalStaticContent);
        Assertions.assertEquals(endingContent, finalReplacedContent);
    }

    @Test
    @DisplayName("Store Policy | Directory Full Swap")
    public void testStorePolicyDirectoryFullSwap() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomDirStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.FULL_SWAP);

        String startingContent = "unit-test-start";
        String endingContent   = "unit-test-end";
        String staticContent   = "unit-test-static";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File staticFile   = manager.getStoreFile(store, entity, "static.txt");
        File replacedFile = manager.getStoreFile(store, entity, "replaced.txt");
        Files.writeString(staticFile.toPath(), staticContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        Files.writeString(replacedFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            try (InputStream is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8))) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, "replaced.txt", is));
            }

            context.commit();
        }

        Assertions.assertFalse(staticFile.exists());
        Assertions.assertTrue(replacedFile.exists());

        String finalReplacedContent = Files.readString(replacedFile.toPath());

        Assertions.assertEquals(endingContent, finalReplacedContent);
    }

    @Test
    @DisplayName("Store Policy | File Overwrite (Content)")
    public void testStorePolicyFileOverwriteContent() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomFileStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.OVERWRITE);

        String startingContent = "unit-test-start";
        String endingContent   = "unit-test-end";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File localFile = manager.getStoreFile(store, entity);
        Files.writeString(localFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            try (InputStream is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8))) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, is));
            }
            context.commit();
        }

        Assertions.assertTrue(localFile.exists());
        String finalContent = Files.readString(localFile.toPath());

        Assertions.assertEquals(endingContent, finalContent);
    }

    @Test
    @DisplayName("Store Policy | File Overwrite (No Content)")
    public void testStorePolicyFileOverwriteNoContent() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomFileStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.OVERWRITE);

        String startingContent = "unit-test-start";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File localFile = manager.getStoreFile(store, entity);
        Files.writeString(localFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            context.commit();
        }

        Assertions.assertTrue(localFile.exists());
        String finalContent = Files.readString(localFile.toPath());

        Assertions.assertEquals(startingContent, finalContent);
    }

    @Test
    @DisplayName("Store Policy | File Full Swap (Content)")
    public void testStorePolicyFileFullSwapContent() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomFileStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.FULL_SWAP);

        String startingContent = "unit-test-start";
        String endingContent   = "unit-test-end";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File localFile = manager.getStoreFile(store, entity);
        Files.writeString(localFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            try (InputStream is = new ByteArrayInputStream(endingContent.getBytes(StandardCharsets.UTF_8))) {
                Assertions.assertDoesNotThrow(() -> context.store(scope, is));
            }
            context.commit();
        }

        Assertions.assertTrue(localFile.exists());
        String finalContent = Files.readString(localFile.toPath());

        Assertions.assertEquals(endingContent, finalContent);
    }

    @Test
    @DisplayName("Store Policy | File Full Swap (No Content)")
    public void testStorePolicyFileFullSwapNoContent() throws IOException {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity     entity = new ScopedEntityA("1");
        FileStore        store  = randomFileStore(ScopedEntityA.class);
        AccessScope      scope  = new AccessScope(store, entity);
        Set<AccessScope> scopes = Set.of(scope);

        manager.register(store, StorePolicy.FULL_SWAP);

        String startingContent = "unit-test-start";

        // Please don't do the following in production code (it defeats isolation, very bad), only allowed during tests :)
        File localFile = manager.getStoreFile(store, entity);
        Files.writeString(localFile.toPath(), startingContent, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        try (FileIsolationContext context = manager.createIsolation(scopes)) {
            context.commit();
        }

        Assertions.assertFalse(localFile.exists());
    }

    @Test
    @DisplayName("File Access Boundaries")
    public void testFileAccessBoundaries() {

        File           testData = new File("test-data");
        File           library  = new File(testData, "library");
        LibraryManager manager  = new LibraryManager(library);

        ScopedEntity entity      = new ScopedEntityA("1");
        String       oobFilename = "../escape-path.txt";
        String       filename    = "normal.txt";
        String       veryMeanOob = ".txt/../../escape-path.txt";

        FileStore scopedStore = randomDirStore(ScopedEntityA.class);
        FileStore rawStore    = randomRaw();

        manager.register(scopedStore, StorePolicy.OVERWRITE);
        manager.register(rawStore, StorePolicy.DISCARD);

        AccessScope scope = new AccessScope(scopedStore, entity);

        File file, storeRoot;

        // Test library boundaries
        storeRoot = Assertions.assertDoesNotThrow(() -> manager.getStoreFile(rawStore));
        file      = Assertions.assertDoesNotThrow(() -> manager.getStoreFile(rawStore, filename));
        Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, file));

        storeRoot = Assertions.assertDoesNotThrow(() -> manager.getStoreFile(scopedStore, entity));
        file      = Assertions.assertDoesNotThrow(() -> manager.getStoreFile(scopedStore, entity, filename));
        Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, file));

        Assertions.assertThrows(StoreBreakoutException.class, () -> manager.getStoreFile(rawStore, oobFilename));
        Assertions.assertThrows(StoreBreakoutException.class, () -> manager.getStoreFile(scopedStore, entity, oobFilename));
        Assertions.assertThrows(StoreBreakoutException.class, () -> manager.getStoreFile(rawStore, veryMeanOob));
        Assertions.assertThrows(StoreBreakoutException.class, () -> manager.getStoreFile(scopedStore, entity, veryMeanOob));

        // Test isolation boundaries
        try (FileIsolationContext context = manager.createIsolation(scope)) {

            storeRoot = Assertions.assertDoesNotThrow(() -> context.getStoreFile(scopedStore, entity));
            file      = Assertions.assertDoesNotThrow(() -> context.getStoreFile(scopedStore, entity, filename));
            Assertions.assertTrue(FileUtils.isDirectChild(storeRoot, file));

            Assertions.assertThrows(StoreBreakoutException.class, () -> context.getStoreFile(scopedStore, entity, oobFilename));
            Assertions.assertThrows(StoreBreakoutException.class, () -> context.requestTemporaryFile(veryMeanOob));
        }

    }


    @AfterEach
    public void cleanup() throws IOException {

        File testData = new File("test-data");
        File library  = new File(testData, "library");
        FileUtils.deleteRecursively(library);
    }

}
