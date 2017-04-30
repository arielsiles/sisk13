package com.encens.khipus.util;

import com.encens.khipus.model.common.DayOfWeek;
import com.encens.khipus.model.employees.Month;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DateUtils
 *
 * @author
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static Boolean dateEquals(Calendar calendarFirst, Calendar calendarSecond){

        if(calendarFirst.get(Calendar.YEAR) == calendarSecond.get(Calendar.YEAR))
            if(calendarFirst.get(Calendar.MONTH) == calendarSecond.get(Calendar.MONTH))
                if(calendarFirst.get(Calendar.DAY_OF_MONTH) == calendarSecond.get(Calendar.DAY_OF_MONTH))
                    return true;

        return false;

    }

    public static Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar toDateCalendar(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }
        return null;
    }

    /**
     * This method remove the time attributes of date object
     *
     * @param date object parameter
     * @return a java.util.Date object without time attributes
     */
    public static Date removeTime(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
        return null;
    }

    /*
   * Returns the date for current date without time's information
   * */
    public static Date toDay() {
        return removeTime(new Date());
    }


    /**
     * This method sets the calendar argument to the the zero hours of the day
     *
     * @param calendar a given parameter
     */
    public static void toMinHours(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
    }

    /**
     * This method sets the calendar argument to the the last hours of the day
     *
     * @param calendar a given parameter
     */
    public static void toMaxHours(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
        }
    }

    public static Calendar joinDateAndTime(Date date, Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        calendar.set(Calendar.SECOND, time.getSeconds());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static Date parse(String dateString, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
        }
        return null;
    }

    public static Boolean isValidPattern(String dateString, String pattern) {
        return parse(dateString, pattern) != null;
    }

    public static int yearsBetween(DateTime initDateTime, DateTime endDateTime) {
        int years = 0;
        if (initDateTime != null && endDateTime != null && initDateTime.isBefore(endDateTime)) {
            Period period = new Period(initDateTime, endDateTime);
            years = period.getYears();
        }
        return years;
    }

    public static long daysBetween(Date initDate, Date endDate) {
        return daysBetween(initDate, endDate, true);
    }

    public static long daysBetween(Date initDate, Date endDate, Boolean includeEndDate) {
        return getDifference(toDateCalendar(initDate), toDateCalendar(endDate), TimeUnit.DAYS) + (includeEndDate ? 1 : 0);
    }

    public static long differenceBetween(Date initDate, Date endDate, TimeUnit units) {
        return differenceBetween(initDate, endDate, units, true);
    }

    public static long differenceBetween(Date initDate, Date endDate, TimeUnit units, Boolean includeEndDate) {
        return getDifference(toCalendar(initDate), toCalendar(endDate), units) + (includeEndDate ? 1 : 0);
    }

    public static long getDifference(Calendar initDate, Calendar endDate, TimeUnit units) {
        return units.convert(endDate.getTimeInMillis() - initDate.getTimeInMillis(), TimeUnit.MILLISECONDS);
    }

    public static long daysBetweenWithoutWeekend(Date initDate, Date endDate) {
        return daysBetweenWithoutWeekend(initDate, endDate, true);
    }

    public static long daysBetweenWithoutWeekend(Date initDate, Date endDate, Boolean includeEndDate) {
        long daysBetween = daysBetween(initDate, endDate, includeEndDate);
        DateTime currentDateTime = (new DateTime(initDate.getTime())).toDateMidnight().toDateTime();
        DateTime endDataTime = (new DateTime(endDate.getTime())).toDateMidnight().toDateTime();
        while (currentDateTime.compareTo(endDataTime) <= 0) {
            if (DayOfWeek.SUNDAY.getDateTimeValue() == currentDateTime.getDayOfWeek()
                    || DayOfWeek.SATURDAY.getDateTimeValue() == currentDateTime.getDayOfWeek()) {
                daysBetween--;
            }
            currentDateTime = currentDateTime.plusDays(1);
        }
        return daysBetween;
    }

    public static Date getFirstDayOfMonth(Date today) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        Calendar firstDayOfMonthCalendar = Calendar.getInstance();
        firstDayOfMonthCalendar.set(
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.getMinimum(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        return firstDayOfMonthCalendar.getTime();
    }

    public static Date getMiddleDayOfMonth(Date today) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        Calendar middleDayOfMonthCalendar = Calendar.getInstance();
        middleDayOfMonthCalendar.set(
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                15,
                0, 0, 0);
        return middleDayOfMonthCalendar.getTime();
    }

    public static Date getFirstMiddleDayOfMonth(Date date){
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(date);

        Calendar firstMiddleDayOfmonthCalendar = Calendar.getInstance();

        firstMiddleDayOfmonthCalendar.set(
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                16,
                0, 0, 0);
        return firstMiddleDayOfmonthCalendar.getTime();
    }

    public static Date getLastDayOfMonth(Date today) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        Calendar lastDayOfMonthCalendar = Calendar.getInstance();
        lastDayOfMonthCalendar.set(
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                23, 59, 59);
        return lastDayOfMonthCalendar.getTime();
    }

    public static boolean isDayInMonth(Date firstDayOfMonth, Date lastDayOfMonth, Date today) {
        return firstDayOfMonth.getTime() <= today.getTime() && today.getTime() <= lastDayOfMonth.getTime();
    }

    /**
     * This method return the difference in months from actual date to date param
     *
     * @param date The date to be compared
     * @return The difference of dates in months
     */
    public static Integer getMonthsDifference(Date date) {
        Period period = new Period(date.getTime(), new Date().getTime());
        Integer res = (period.getYears() * 12) + period.getMonths();
        return (res);
    }

    public static Date lastDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int lastDate = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, lastDate);
        return calendar.getTime();
    }

    public static Date firstDayOfMonth(Integer month, Integer year) {
        Date d = new Date();
        d.setDate(1);
        d.setYear(year - 1900);
        d.setMonth(month);
        d.setHours(0);
        d.setMinutes(0);
        return d;
    }

    public static Date getFirstDayOfMonth(Integer month, Integer year) {
        DateTime dateTime = new DateTime(year, month, 1, 0, 0, 1, 0);
        return dateTime.toDate();
    }

    public static Date getLastDayOfMonth(Long timeMillis) {
        DateTime dateTime = new DateTime(timeMillis);
        return dateTime.dayOfMonth().withMaximumValue().toDate();
    }

    public static Date firstDayOfYear(Integer year) {
        Date d = new Date();
        d.setMonth(0);
        d.setDate(1);
        d.setYear(year - 1900);
        d.setHours(0);
        d.setMinutes(0);
        return d;
    }

    public static Date lastDayOfYear(Integer year) {
        Date d = new Date();
        d.setMonth(11);
        d.setYear(year - 1900);
        d.setHours(23);
        d.setMinutes(59);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        int lastDate = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, lastDate);
        return calendar.getTime();
    }

    public static Date lastDayOfMonth(Integer month, Integer year) {
        Date d = new Date();
        d.setMonth(month);
        d.setYear(year - 1900);
        d.setHours(23);
        d.setMinutes(59);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        int lastDate = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, lastDate);
        return calendar.getTime();
    }

    public static Integer getCurrentYear(Date date) {
        DateTime dateTime = new DateTime(date.getTime());
        return dateTime.getYear();
    }

    public static Integer getCurrentMonth(Date date) {
        DateTime dateTime = new DateTime(date.getTime());
        return dateTime.getMonthOfYear();
    }

    public static Date getDate(Integer year, Integer month) {
        DateTime today = new DateTime();
        DateTime dateTime = new DateTime(year,
                month,
                today.getDayOfMonth(),
                today.getHourOfDay(),
                today.getMinuteOfHour(),
                today.getSecondOfMinute(), 0);

        return dateTime.toDate();
    }

    public static Date getDate(Integer year, Integer month, Integer day) {
        DateTime dateTime = new DateTime(year, month, day, 0, 0, 0, 0);
        return dateTime.toDate();
    }

    public static Integer dateToInteger(Date date) {
        DateTime dateTime = new DateTime(date.getTime());

        Integer year = dateTime.getYear();
        Integer month = dateTime.getMonthOfYear();
        Integer day = dateTime.getDayOfMonth();

        String yearAsString = year.toString();
        String monthAsString = month.toString();
        if (month < 10) {
            monthAsString = "0" + month;
        }
        String dayAsString = day.toString();
        if (day < 10) {
            dayAsString = "0" + day;
        }

        return Integer.valueOf(yearAsString + monthAsString + dayAsString);
    }

    /**
     * Build a map of day frequency per month given a range of dates
     *
     * @param initDate the init date of the range
     * @param endDate  the end date of the range
     * @return a map of day frequency per month given a range of dates
     */
    public Map<Month, Map<DayOfWeek, Integer>> monthlyDayFrequencyMap(Date initDate, Date endDate) {
        Map<Month, Map<DayOfWeek, Integer>> monthlyDayFrequencyMap = new HashMap<Month, Map<DayOfWeek, Integer>>();
        Calendar end = DateUtils.toCalendar(endDate);
        Calendar firstDayOfRangeMonth = DateUtils.toDateCalendar(initDate);
        firstDayOfRangeMonth.set(Calendar.DAY_OF_MONTH, firstDayOfRangeMonth.getActualMinimum(Calendar.DAY_OF_MONTH));

        Calendar lastDayOfRangeMonth = DateUtils.toDateCalendar(endDate);
        lastDayOfRangeMonth.set(Calendar.DAY_OF_MONTH, lastDayOfRangeMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        // load months map and init it
        while (firstDayOfRangeMonth.before(lastDayOfRangeMonth)) {
            int month = firstDayOfRangeMonth.get(Calendar.MONTH);
            Map<DayOfWeek, Integer> dayMap = new HashMap<DayOfWeek, Integer>();
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                dayMap.put(dayOfWeek, 0);
            }
            monthlyDayFrequencyMap.put(Month.getMonthByCalendarIndex(month), dayMap);

            firstDayOfRangeMonth.add(Calendar.MONTH, 1);
        }

        Calendar dateIterator = DateUtils.toDateCalendar(initDate);
        while (dateIterator.compareTo(end) <= 0) {
            Map<DayOfWeek, Integer> monthMap = monthlyDayFrequencyMap.get(Month.getMonthByCalendarIndex(dateIterator.get(Calendar.MONTH)));
            int dayOfWeekIndex = dateIterator.get(Calendar.DAY_OF_WEEK);
            DayOfWeek dayOfWeek = DayOfWeek.getDayOfWeek(dayOfWeekIndex);
            monthMap.put(dayOfWeek, (monthMap.get(dayOfWeek) + 1));
            dateIterator.add(Calendar.DAY_OF_MONTH, 1);
        }
        return monthlyDayFrequencyMap;
    }

    public static Date getLastDayFromPeriod(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(calendar.get(Calendar.DAY_OF_MONTH) > 15 ){
            return getLastDayOfMonth(date);
        }else{
            return getMiddleDayOfMonth(date);
        }
    }

    public static Date getFirsDayFromPeriod(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(calendar.get(Calendar.DAY_OF_MONTH) > 15){
            return getFirstMiddleDayOfMonth(date);
        }else{
            return getFirstDayOfMonth(date);
        }

    }

    public static boolean isMajor(Calendar date, Calendar dateCompare) {
        if(date.get(Calendar.YEAR) <= dateCompare.get(Calendar.YEAR))
            if(date.get(Calendar.MONTH) <= dateCompare.get(Calendar.MONTH))
                if(date.get(Calendar.DAY_OF_MONTH) <= dateCompare.get(Calendar.DAY_OF_MONTH))
                    return false;
        return true;
    }

    public static boolean isLess(Calendar date, Calendar dateCompare) {
        if(date.get(Calendar.YEAR) >= dateCompare.get(Calendar.YEAR))
            if(date.get(Calendar.MONTH) >= dateCompare.get(Calendar.MONTH))
                if(date.get(Calendar.DAY_OF_MONTH) >= dateCompare.get(Calendar.DAY_OF_MONTH))
                    return false;
        return true;
    }
}
