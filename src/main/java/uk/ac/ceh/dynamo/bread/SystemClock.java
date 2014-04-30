package uk.ac.ceh.dynamo.bread;

import java.util.Calendar;

/**
 *
 * @author Christopher Johnson
 */
public class SystemClock implements Clock {

    @Override
    public long getTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

}
