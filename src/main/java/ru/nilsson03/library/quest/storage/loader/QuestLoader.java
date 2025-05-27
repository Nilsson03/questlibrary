package ru.nilsson03.library.quest.storage.loader;

import com.google.common.base.Preconditions;
import ru.nilsson03.library.quest.core.Quest;

import java.io.File;
import java.io.IOException;
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
    default List<Quest> loadQuests(File questsDirectory) {
        Preconditions.checkArgument(questsDirectory.isDirectory(), "questsDirectory must be a directory");

        File[] files = questsDirectory.listFiles();
        Objects.requireNonNull(files, "questsDirectory.listFiles() is null");

        List<Quest> quests = new ArrayList<>();

        for (File file : files) {

            if (!file.canRead()) {
                continue;
            }

            Quest quest = loadQuestFromFile(file);
            quests.add(quest);
        }

        return quests;
    }

    /**
     * Загружает квест из файла.
     *
     * @param file файл, содержащий данные квеста
     * @return объект Quest или null, если файл не содержит валидных данных
     * @throws IOException если произошла ошибка при чтении файла
     */
    Quest loadQuestFromFile(File file);
}
