package com.encens.khipus.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Ariel Siles Encinas
 * Date: 30-07-2009
 * Time: 05:16:49 PM
 */
public class DateIterator implements Iterator<Date>, Iterable<Date> {

    private Calendar start = Calendar.getInstance();
    private Calendar end = Calendar.getInstance();
    private Calendar current = Calendar.getInstance();

    public DateIterator(Date start, Date end) {
        this.start.setTime(start);
        this.end.setTime(end);
        this.current.setTime(start);
    }

    public boolean hasNext() {
        return !current.after(end);
    }

    public Date next() {
        current.add(Calendar.DATE, 1);
        return current.getTime();
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove");
    }

    public Iterator<Date> iterator() {
        return this;
    }

    public Date getCurrent() {
        return this.current.getTime();
    }

}
