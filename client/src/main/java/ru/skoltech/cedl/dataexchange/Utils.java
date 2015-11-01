package ru.skoltech.cedl.dataexchange;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static final long INVALID_TIME = - 1;

    public static final DateFormat TIME_AND_DATE_FOR_FILENAMES = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    public static final DateFormat TIME_AND_DATE_FOR_USER_INTERFACE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static String getFormattedDateAndTime() {
        Date now = new Date();
        return TIME_AND_DATE_FOR_FILENAMES.format(now);
    }

    public static <T> T copyBean(T source, T target) {
        try {
            PropertyUtils.copyProperties(target, source);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("error copying bean " + source.toString(), e);
        }
        return target;
    }
}
