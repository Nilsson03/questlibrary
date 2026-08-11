package ru.nilsson03.library.quest.core.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.event.player.PlayerExpChangeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.LevelCondition;
import ru.nilsson03.library.quest.core.event.UserQuestProgressEvent;
import ru.nilsson03.library.quest.meta.impl.SimpleQuestMeta;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.quest.simple.impl.BaseQuestImpl;
import ru.nilsson03.library.quest.user.data.QuestSubjectKind;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.impl.BaseQuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

class GroupProgressSupportTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void setProgressWorksForGroupOwnerWithOnlineActor() {
        PlayerMock actor = server.addPlayer("contributor");
        UUID guildId = UUID.randomUUID();

        QuestUserData guild = new BaseQuestUserData(
                guildId, QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());
        BaseQuest quest = sampleQuest(Collections.emptySet());
        Objective objective = quest.objectives().get(0);
        NumericGoal goal = (NumericGoal) objective.goals().get(0);

        BaseQuestProgress progress = new BaseQuestProgress(guild, quest, objective);
        AtomicReference<UserQuestProgressEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onProgress(UserQuestProgressEvent event) {
                captured.set(event);
            }
        }, MockBukkit.createMockPlugin());

        progress.incrementProgress(goal, 5, actor);

        assertEquals(5L, progress.getValue(goal));
        UserQuestProgressEvent event = captured.get();
        assertTrue(event != null);
        assertEquals(5L, event.getDelta());
        assertEquals(actor.getUniqueId(), event.getActorId().orElse(null));
        assertEquals(guildId, event.getQuestUserData().uuid());
    }

    @Test
    void setProgressWithoutActorDoesNotAdvanceGroupOwner() {
        UUID guildId = UUID.randomUUID();
        QuestUserData guild = new BaseQuestUserData(
                guildId, QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());
        BaseQuest quest = sampleQuest(Collections.emptySet());
        Objective objective = quest.objectives().get(0);
        NumericGoal goal = (NumericGoal) objective.goals().get(0);
        BaseQuestProgress progress = new BaseQuestProgress(guild, quest, objective);

        progress.incrementProgress(goal, 3);

        assertEquals(0L, progress.getValue(goal));
    }

    @Test
    void progressTargetResolverMapsActorToGroupOwner() {
        PlayerMock actor = server.addPlayer("member");
        UUID guildId = UUID.randomUUID();
        QuestUserData guild = new BaseQuestUserData(
                guildId, QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());

        QuestUsersStorage storage = mock(QuestUsersStorage.class);
        when(storage.getQuestUserData(guildId)).thenReturn(guild);

        ProgressTargetResolver resolver = ProgressTargetResolver.mapping(storage, playerId -> guildId);
        QuestUserData resolved = resolver.resolve(actor);

        assertEquals(guild, resolved);
        assertEquals(QuestSubjectKind.GROUP, resolved.subjectKind());
    }

    @Test
    void playerConditionsUseActorForGroupOwner() {
        PlayerMock actor = server.addPlayer("member");
        actor.setLevel(20);

        QuestUserData guild = new BaseQuestUserData(
                UUID.randomUUID(), QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());
        LevelCondition condition = new LevelCondition(10, 30, QuestCondition.ConditionType.PROGRESS);

        assertTrue(condition.isMet(ConditionContext.of(guild, actor)));
        assertFalse(condition.isMet(ConditionContext.of(guild)));
    }

    @Test
    void startPlayerConditionsAreSkippedForGroupWithoutActor() {
        QuestUserData guild = new BaseQuestUserData(
                UUID.randomUUID(), QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());
        LevelCondition condition = new LevelCondition(50, Integer.MAX_VALUE, QuestCondition.ConditionType.START);

        assertTrue(condition.isMet(ConditionContext.of(guild)));
    }

    @Test
    void clearCachedQuestDataClearsGroupOwnerDailyProgress() {
        UUID guildId = UUID.randomUUID();
        QuestUserData guild = new BaseQuestUserData(
                guildId, QuestSubjectKind.GROUP, new ArrayList<>(), new ArrayList<>());
        BaseQuest quest = sampleQuest(Collections.emptySet());
        Objective objective = quest.objectives().get(0);
        BaseQuestProgress progress = new BaseQuestProgress(guild, quest, objective);
        guild.addActiveQuests(List.of(progress));
        guild.addCompletedQuest(quest);

        QuestUsersStorage storage = mock(QuestUsersStorage.class);
        assertTrue(guild.isActiveQuest(quest));
        assertTrue(guild.questIsComplete(quest));
        guild.clearQuestState(quest);
        assertFalse(guild.isActiveQuest(quest));
        assertFalse(guild.questIsComplete(quest));
    }

    private static BaseQuest sampleQuest(Set<QuestCondition> conditions) {
        ObjectiveType type = ObjectiveType.create("TEST_EXP", PlayerExpChangeEvent.class);
        NumericGoal goal = new NumericGoal(10);
        Objective objective = new Objective("obj", type, List.of(), List.of(goal), "test");
        return new BaseQuestImpl(
                Namespace.of("TestPlugin", "sample_quest"),
                new SimpleQuestMeta(1, List.of("desc"), "Sample"),
                conditions,
                List.of(objective),
                null);
    }
}
