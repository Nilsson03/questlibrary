package ru.nilsson03.library.quest.objective.goal;

public interface Goal {

    /**
     * Метод для проверки, соответствует ли объект цели заданному объекту.
     *
     * @param target объект, с которым необходимо провести сравнение.
     * @return true, если объекты совпадают, в противном случае false.
     */
    boolean matches(Object target);

    long targetValue();
}
