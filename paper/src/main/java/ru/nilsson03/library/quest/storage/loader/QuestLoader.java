package ru.nilsson03.library.quest.storage.loader;

import com.google.common.base.Preconditions;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface QuestLoader {

    /**
     * Загружает квесты из файлов в указанной директории.
     *
     * @param questsDirectory директория, содержащая файлы с квестами
     * @return список загруженных квестов
     * @throws IllegalArgumentException если questsDirectory не является директорией
     */
    default List<BaseQuest> loadQuests(File questsDirectory) {
        Preconditions.checkArgument(questsDirectory.isDirectory(), "questsDirectory must be a directory");

        File[] files = questsDirectory.listFiles();
        Objects.requireNonNull(files, "questsDirectory.listFiles() is null");

        List<BaseQuest> quests = new ArrayList<>();

        for (File file : files) {

            if (!file.canRead()) {
                continue;
            }

            BaseQuest quest = loadQuestFromFile(file);
            quests.add(quest);
        }

        return quests;
    }

    /**
     * Загружает квест из файла.
     *
     * @param file файл, содержащий данные квеста
     * @return объект Quest или null, если файл не содержит валидных данных
     */
    BaseQuest loadQuestFromFile(File file);
}
