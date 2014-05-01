package uk.ac.ceh.dynamo.bread;

import java.util.Calendar;

/**
 * The standard implementation of a clock, this is based on system time and
 * therefore useful in production systems
 * @author Christopher Johnson
 */
public class SystemClock implements Clock {

    /**
     * @return the current system time in UTC milliseconds
     */
    @Override
    public long getTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
