package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.core.QuestSystemContext;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.daily.persistence.InMemoryDailyQuestPersistence;
import ru.nilsson03.library.quest.meta.QuestRarity;
import ru.nilsson03.library.quest.meta.impl.RarityQuestMeta;
import ru.nilsson03.library.quest.meta.parser.registry.MetaParserRegistry;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

class DailyQuestSystemTest {

    @TempDir
    File tempDir;

    private ServerMock server;
    private NPlugin plugin;
    private QuestSystemContext context;
    private QuestStorage mainStorage;
    private InMemoryDailyQuestPersistence persistence;
    private List<BaseQuest> poolQuests;
    private QuestLoader poolLoader;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        plugin = mock(NPlugin.class);
        when(plugin.getName()).thenReturn("TestPlugin");
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getServer()).thenReturn(server);

        poolQuests = List.of(
                quest("easy_1", "EASY"),
                quest("easy_2", "EASY"),
                quest("hard_1", "HARD"),
                quest("epic_1", "EPIC"),
                quest("master_1", "MASTER"));

        poolLoader = fixedLoader(poolQuests);
        mainStorage = new QuestStorage(plugin, fixedLoader(List.of()));
        QuestService questService = mock(QuestService.class);
        MetaParserRegistry metaRegistry = mock(MetaParserRegistry.class);
        when(metaRegistry.isParserRegistered("rarity")).thenReturn(true);
        when(questService.getPlugin()).thenReturn(plugin);
        when(questService.getMetaParserRegistry()).thenReturn(metaRegistry);

        QuestUsersStorage usersStorage = mock(QuestUsersStorage.class);
        UserDataPersistent userPersistent = mock(UserDataPersistent.class);
        when(userPersistent.deleteQuestDataByKeys(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(userPersistent.deleteQuestData(any(UUID.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        context = mock(QuestSystemContext.class);
        when(context.getQuestService()).thenReturn(questService);
        when(context.getQuestStorage()).thenReturn(mainStorage);
        when(context.getQuestUsersStorage()).thenReturn(usersStorage);
        when(context.getDataPersistent()).thenReturn(userPersistent);

        persistence = new InMemoryDailyQuestPersistence();
        Files.createDirectories(tempDir.toPath().resolve("daily_quests"));

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("limit", 3);
        yaml.set("update-period", "1d");
        yaml.set("assignment-mode", "SHARED");
        yaml.set("rarities.EASY", 50);
        yaml.set("rarities.HARD", 30);
        yaml.set("rarities.EPIC", 15);
        yaml.set("rarities.MASTER", 5);
        yaml.save(new File(tempDir, "daily_quests.yml"));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void sharedModeGivesSameQuestsToAllPlayers() {
        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(123))
                .build();
        system.start();

        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        List<BaseQuest> forA = system.getActiveDailyQuests(playerA);
        List<BaseQuest> forB = system.getActiveDailyQuests(playerB);

        assertEquals(3, forA.size());
        assertEquals(forA, forB);
        assertTrue(mainStorage.getQuests().containsAll(poolQuests));
    }

    @Test
    void personalModeAssignsIndependently() {
        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.PERSONAL)
                .limit(2)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(99))
                .build();
        system.start();

        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        List<BaseQuest> forA = system.getActiveDailyQuests(playerA);
        List<BaseQuest> forB = system.getActiveDailyQuests(playerB);

        assertEquals(2, forA.size());
        assertEquals(2, forB.size());
        assertEquals(forA, system.getActiveDailyQuests(playerA));
        assertFalse(forA.isEmpty());
        assertFalse(forB.isEmpty());
    }

    @Test
    void bootstrapCreatesConfigInConsumerDataFolderOnly() {
        File config = new File(tempDir, "daily_quests.yml");
        assertTrue(config.exists());

        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .schedulerEnabled(false)
                .build();
        system.start();

        File folder = new File(tempDir, "daily_quests");
        assertTrue(folder.isDirectory());
        assertEquals(tempDir, plugin.getDataFolder());
    }

    @Test
    void forceRotateClearsCachedProgressIncludingGroupOwners() {
        QuestUsersStorage usersStorage = context.getQuestUsersStorage();
        org.mockito.Mockito.reset(usersStorage);

        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(123))
                .build();
        system.start();

        List<BaseQuest> before = system.getSharedActiveSnapshot();
        system.clearDailyProgressForOwners(before);

        org.mockito.Mockito.verify(usersStorage).clearCachedQuestData(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void forceRotateChangesSharedSelectionEvenWhenPeriodNotExpired() {
        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(123))
                .build();
        system.start();

        List<BaseQuest> before = system.getSharedActiveSnapshot();
        assertEquals(3, before.size());

        system.forceRotate();

        List<BaseQuest> after = system.getSharedActiveSnapshot();
        assertEquals(3, after.size());
        assertEquals(after, system.getActiveDailyQuests(UUID.randomUUID()));
        assertEquals(after, system.getActiveDailyQuests(UUID.randomUUID()));
    }

    @Test
    void expiredPeriodRotatesOnGetActiveDailyQuests() {
        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1s")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(42))
                .build();
        system.start();

        List<BaseQuest> initial = List.copyOf(system.getSharedActiveSnapshot());
        long stale = System.currentTimeMillis() - 5_000L;
        persistence.saveSharedQuests(initial, stale);

        List<BaseQuest> afterExpiry = system.getActiveDailyQuests(UUID.randomUUID());
        assertEquals(3, afterExpiry.size());
        assertTrue(system.getLastUpdateTime() > stale);
    }

    @Test
    void activeQuestsUnchangedWhenPeriodNotExpired() {
        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(7))
                .build();
        system.start();

        List<BaseQuest> first = system.getActiveDailyQuests(UUID.randomUUID());
        long lastUpdate = system.getLastUpdateTime();
        List<BaseQuest> second = system.getActiveDailyQuests(UUID.randomUUID());

        assertEquals(first, second);
        assertEquals(lastUpdate, system.getLastUpdateTime());
    }

    @Test
    void forceRotateClearsProgressForPreviousSharedQuests() {
        QuestUsersStorage usersStorage = context.getQuestUsersStorage();
        UserDataPersistent userPersistent = context.getDataPersistent();

        DailyQuestSystem system = DailyQuestSystem.builder(context)
                .withQuestLoader(poolLoader)
                .withPersistence(persistence)
                .assignmentMode(DailyAssignmentMode.SHARED)
                .limit(3)
                .updatePeriod("1d")
                .schedulerEnabled(false)
                .withRandom(new java.util.Random(11))
                .build();
        system.start();

        List<BaseQuest> previous = system.getSharedActiveSnapshot();
        system.forceRotate();

        org.mockito.Mockito.verify(usersStorage).clearCachedQuestData(previous);
        org.mockito.Mockito.verify(userPersistent).deleteQuestDataByKeys(org.mockito.ArgumentMatchers.argThat(keys -> {
            if (keys == null || keys.size() != previous.size()) {
                return false;
            }
            return previous.stream()
                    .map(quest -> quest.questUniqueKey().getKey())
                    .allMatch(keys::contains);
        }));
    }

    private static QuestLoader fixedLoader(List<BaseQuest> quests) {
        return new QuestLoader() {
            @Override
            public BaseQuest loadQuestFromFile(File file) {
                return null;
            }

            @Override
            public List<BaseQuest> loadQuests(File questsDirectory) {
                return quests;
            }
        };
    }

    private static BaseQuest quest(String key, String rarity) {
        BaseQuest quest = mock(BaseQuest.class);
        when(quest.questUniqueKey()).thenReturn(Namespace.of("TestPlugin", key));
        when(quest.questMeta()).thenReturn(new RarityQuestMeta(
                QuestRarity.of(rarity), key, Material.STONE, List.of()));
        when(quest.toString()).thenReturn(key);
        return quest;
    }
}
