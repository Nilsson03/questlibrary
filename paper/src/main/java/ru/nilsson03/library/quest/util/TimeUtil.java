package ru.nilsson03.library.quest.util;


import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public final class TimeUtil {

    private static final long SECONDS_IN_MINUTE = 60;
    private static final long SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    private static final long SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
    private static final long SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;
    private static final long SECONDS_IN_MONTH = (long) (SECONDS_IN_DAY * 30.44);
    private static final long SECONDS_IN_YEAR = (long) (SECONDS_IN_DAY * 365.24);

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Moscow");

    private TimeUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Преобразует время в секундах в строковое представление (краткий формат)
     * @param time время в секундах
     * @return строковое представление времени (например, "1d 2H 30m")
     */
    public static String parseTimeToString(long time) {
        if (time <= 0) return "0s";

        long seconds = time % SECONDS_IN_MINUTE;
        long minutes = (time / SECONDS_IN_MINUTE) % 60;
        long hours = (time / SECONDS_IN_HOUR) % 24;
        long days = (time / SECONDS_IN_DAY) % 7;
        long weeks = (time / SECONDS_IN_WEEK) % 4;
        long months = (time / SECONDS_IN_MONTH) % 12;
        long years = time / SECONDS_IN_YEAR;

        StringBuilder str = new StringBuilder();
        if (years > 0) str.append(years).append("y ");
        if (months > 0) str.append(months).append("M ");
        if (weeks > 0) str.append(weeks).append("w ");
        if (days > 0) str.append(days).append("d ");
        if (hours > 0) str.append(hours).append("H ");
        if (minutes > 0) str.append(minutes).append("m ");
        if (seconds > 0 || str.length() == 0) str.append(seconds).append("s");

        return str.toString().trim();
    }

    /**
     * Преобразует время в секундах в читаемое строковое представление с полными названиями единиц времени
     * @param seconds время в секундах
     * @return строковое представление времени (например, "1 час, 30 минут")
     */
    public static String getTime(long seconds) {
        if (seconds <= 0) return "0 секунд";

        long years = seconds / SECONDS_IN_YEAR;
        seconds -= years * SECONDS_IN_YEAR;

        long months = seconds / SECONDS_IN_MONTH;
        seconds -= months * SECONDS_IN_MONTH;

        long weeks = seconds / SECONDS_IN_WEEK;
        seconds -= weeks * SECONDS_IN_WEEK;

        long days = seconds / SECONDS_IN_DAY;
        seconds -= days * SECONDS_IN_DAY;

        long hours = seconds / SECONDS_IN_HOUR;
        seconds -= hours * SECONDS_IN_HOUR;

        long minutes = seconds / SECONDS_IN_MINUTE;
        seconds -= minutes * SECONDS_IN_MINUTE;

        StringBuilder sb = new StringBuilder();
        appendTimeUnit(sb, years, Unit.YEARS);
        appendTimeUnit(sb, months, Unit.MONTHS);
        appendTimeUnit(sb, weeks, Unit.WEEKS);
        appendTimeUnit(sb, days, Unit.DAYS);
        appendTimeUnit(sb, hours, Unit.HOURS);
        appendTimeUnit(sb, minutes, Unit.MINUTES);

        if (seconds > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(seconds).append(" ").append(getTimeUnitName(seconds, Unit.SECONDS));
        }

        return sb.toString();
    }

    private static void appendTimeUnit(StringBuilder sb, long value, Unit unit) {
        if (value > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(value).append(" ").append(getTimeUnitName(value, unit));
        }
    }

    /**
     * Парсит строку с временем в количество секунд
     * @param str строка времени (например, "1d 2H 30m")
     * @return количество секунд
     * @throws IllegalArgumentException если строка имеет неверный формат
     */
    public static long parseStringToTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }

        long time = 0;
        String[] array = str.split(" ");

        for (String element : array) {
            try {
                if (element.contains("s")) {
                    time += parseTimeValue(element, "s");
                } else if (element.contains("m")) {
                    time += parseTimeValue(element, "m") * SECONDS_IN_MINUTE;
                } else if (element.contains("H")) {
                    time += parseTimeValue(element, "H") * SECONDS_IN_HOUR;
                } else if (element.contains("d")) {
                    time += parseTimeValue(element, "d") * SECONDS_IN_DAY;
                } else if (element.contains("w")) {
                    time += parseTimeValue(element, "w") * SECONDS_IN_WEEK;
                } else if (element.contains("M")) {
                    time += parseTimeValue(element, "M") * SECONDS_IN_MONTH;
                } else if (element.contains("y")) {
                    time += parseTimeValue(element, "y") * SECONDS_IN_YEAR;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time format: " + element, e);
            }
        }

        return time;
    }

    private static long parseTimeValue(String element, String suffix) {
        return Long.parseLong(element.replace(suffix, ""));
    }

    /**
     * Форматирует дату по заданному шаблону
     * @param date дата для форматирования
     * @param format шаблон формата (см. SimpleDateFormat)
     * @return отформатированная строка
     * @throws IllegalArgumentException если date или format null
     */
    public static String formatDate(Date date, String format) {
        Objects.requireNonNull(date, "Date cannot be null");
        Objects.requireNonNull(format, "Format cannot be null");

        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Возвращает строку, представляющую время, прошедшее с указанной даты (например, "2 дня назад")
     * @param date исходная дата
     * @return строковое представление прошедшего времени
     */
    public static String getTimeAgo(Date date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return getTime(dateToLocalDateTime(date), getDuration(date));
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Вычисляет разницу между двумя моментами времени
     * @param dateTime начальное время
     * @param seconds количество секунд для добавления
     * @return строковое представление временного интервала
     */
    public static String getTime(LocalDateTime dateTime, long seconds) {
        Objects.requireNonNull(dateTime, "DateTime cannot be null");

        if (seconds <= 0) return "0 секунд";

        LocalDateTime endDateTime = dateTime.plusSeconds(seconds);
        return formatDuration(dateTime, endDateTime);
    }

    private static String formatDuration(LocalDateTime start, LocalDateTime end) {
        long years = ChronoUnit.YEARS.between(start, end);
        start = start.plusYears(years);

        long months = ChronoUnit.MONTHS.between(start, end);
        start = start.plusMonths(months);

        long days = ChronoUnit.DAYS.between(start, end);
        start = start.plusDays(days);

        long hours = ChronoUnit.HOURS.between(start, end);
        start = start.plusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(start, end);
        start = start.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(start, end);

        StringBuilder sb = new StringBuilder();
        appendTimeUnit(sb, years, Unit.YEARS);
        appendTimeUnit(sb, months, Unit.MONTHS);
        appendTimeUnit(sb, days, Unit.DAYS);
        appendTimeUnit(sb, hours, Unit.HOURS);
        appendTimeUnit(sb, minutes, Unit.MINUTES);
        if (seconds > 0) {
            appendTimeUnit(sb, seconds, Unit.SECONDS);
        }

        return sb.toString();
    }

    /**
     * Возвращает правильную форму слова для единицы времени
     * @param value количество
     * @return строковое представление единицы времени в правильной форме
     */
    private static String getTimeUnitName(long value, Unit unit) {
        return getCorrectForm(value, unit.one, unit.few, unit.many);
    }

    private static String getCorrectForm(long number, String form1, String form2, String form5) {
        long n = Math.abs(number) % 100;
        long n1 = n % 10;

        if (n > 10 && n < 20) return form5;
        if (n1 == 1) return form1;
        if (n1 >= 2 && n1 <= 4) return form2;
        return form5;
    }

    /**
     * Вычисляет продолжительность между указанной датой и текущим моментом
     * @param date исходная дата
     * @return продолжительность в секундах
     */
    public static long getDuration(Date date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return Duration.between(date.toInstant(), Instant.now()).getSeconds();
    }

    /**
     * Вычисляет продолжительность между указанной датой в строке и текущим моментом
     * @param date строка с датой в формате ISO-8601
     * @return продолжительность в секундах
     * @throws IllegalArgumentException если строка даты имеет неверный формат
     */
    public static long getDuration(String date) {
        Objects.requireNonNull(date, "Date string cannot be null");
        return Duration.between(parseDate(date).atZone(DEFAULT_ZONE_ID).toInstant(), Instant.now()).getSeconds();
    }

    /**
     * Парсит строку с датой в формате ISO-8601 в LocalDateTime
     * @param date строка с датой
     * @return объект LocalDateTime
     * @throws IllegalArgumentException если строка даты имеет неверный формат
     */
    public static LocalDateTime parseDate(String date) {
        Objects.requireNonNull(date, "Date string cannot be null");
        return Instant.parse(date).atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    /**
     * Возвращает текущую дату
     * @return текущая дата
     */
    public static Date getCurrentDate() {
        return new Date();
    }
    private enum Unit {
        SECONDS("секунда", "секунды", "секунд"),
        MINUTES("минута", "минуты", "минут"),
        HOURS("час", "часа", "часов"),
        DAYS("день", "дня", "дней"),
        WEEKS("неделя", "недели", "недель"),
        MONTHS("месяц", "месяца", "месяцев"),
        YEARS("год", "года", "лет");

        private final String one;
        private final String few;
        private final String many;

        Unit(String one, String few, String many) {
            this.one = one;
            this.few = few;
            this.many = many;
        }
    }
}
