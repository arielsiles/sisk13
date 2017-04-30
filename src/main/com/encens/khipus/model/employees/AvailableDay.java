package com.encens.khipus.model.employees;

/**
 * Encens Team
 *
 * @author
 * @version : AvailableDay, 26-11-2009 08:16:24 PM
 */
public enum AvailableDay {

    MONDAY("AvailableDay.monday", 2),
    TUESDAY("AvailableDay.tuesday", 3),
    WEDNESDAY("AvailableDay.wednesday", 4),
    THURSDAY("AvailableDay.thursday", 5),
    FRIDAY("AvailableDay.friday", 6),
    SATURDAY("AvailableDay.saturday", 7),
    SUNDAY("AvailableDay.sunday", 1);

    private String resourceKey;
    private Integer dayOfWeek;

    AvailableDay(String resourceKey, Integer dayOfWeek) {
        this.resourceKey = resourceKey;
        this.dayOfWeek = dayOfWeek;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public static AvailableDay findByDayOfWeek(Integer dayOfWeek) {
        for (AvailableDay availableDay : values()) {
            if (availableDay.getDayOfWeek().equals(dayOfWeek)) {
                return availableDay;
            }
        }
        return null;
    }
}
