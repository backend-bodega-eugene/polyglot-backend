package com.common.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期工具类
 */
@Slf4j
public class DateUtils {
    private DateUtils() {
    }

    private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter dateNumberFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter timeNumberFormatter = DateTimeFormatter.ofPattern("HHmmss");

    private static final DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter longNumberFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter maxFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final DateTimeFormatter itemTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    public static Date parseDateTime(String str, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseBeginDate(String str, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDateTime = LocalDate.parse(str, formatter);
        return Date.from(localDateTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseEndDate(String str, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDateTime = LocalDate.parse(str, formatter);
        localDateTime = localDateTime.plusDays(1L);
        return Date.from(localDateTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String localDateFormat(Date date) {
        return localDateFormat(convert(date));
    }

    public static String dateFormat(Date date) {
        return dateFormat(convert(date));
    }

    public static String itemItemFormat(Date date) {
        return itemTimeFormat(convert(date));
    }

    public static String localDateFormat(LocalDateTime date) {
        return date.format(localDateFormatter);
    }

    public static String dateFormat(LocalDateTime date) {
        return date.format(dateFormatter);
    }

    public static String itemTimeFormat(LocalDateTime date) {
        return date.format(itemTimeFormatter);
    }

    public static String dateNumberFormat(Date date) {
        return dateNumberFormat(convert(date));
    }

    public static String dateNumberFormat(LocalDateTime date) {
        return date.format(dateNumberFormatter);
    }

    public static String timeFormat(Date date) {
        return timeFormat(convert(date));
    }

    public static String timeFormat(LocalDateTime date) {
        return date.format(timeFormatter);
    }

    public static String timeNumberFormat(Date date) {
        return timeNumberFormat(convert(date));
    }

    public static String timeNumberFormat(LocalDateTime date) {
        return date.format(timeNumberFormatter);
    }

    public static String longFormat(Date date) {
        return longFormat(convert(date));
    }

    public static String longFormat(LocalDateTime date) {
        return date.format(longFormatter);
    }

    public static String longNumberFormat(Date date) {
        return longNumberFormat(convert(date));
    }

    public static String longNumberFormat(LocalDateTime date) {
        return date.format(longNumberFormatter);
    }

    public static String maxFormat(Date date) {
        return maxFormat(convert(date));
    }

    public static String maxFormat(LocalDateTime date) {
        return date.format(maxFormatter);
    }

    public static LocalDateTime convert(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date merge(Date date, Date time) {
        return new Date(date.getTime() + time.getTime());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static void main(String[] args) {
        log.warn("dayStartTime={}", LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN));
        log.warn("dayEndTime={}", LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MAX));
        String dateStr = "2012-10-10 17:21:20";
        String format = "yyyy-MM-dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss
//        log.warn("parseDateTime: " + parseDateTime(dateStr, format));
//        log.warn("parseDateTime: " + parseBeginDate(dateStr, format));
//        log.warn("parseDateTime: " + parseEndDate(dateStr, format));
//        log.warn("parseBeginDate: " + longFormat(new Date()));
        LocalDate date = LocalDate.now();
        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth()); // 获取当前月的第一天
        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth()); // 获取当前月的最后一天
        LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        var ree = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        var re = Date.from(firstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        System.out.println(ree);
        System.out.println(lastDay);
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime day = localDateTime.with(LocalTime.MAX);
        log.warn("day.max={}", longFormat(day));

        log.warn("{}", convert(new Date()));
//        log.warn("开始时间：{}",merge(new Date(),new Date(LocalTime.MIN)));
    }

}