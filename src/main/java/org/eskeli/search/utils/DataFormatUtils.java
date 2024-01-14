package org.eskeli.search.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class Desc : Data formats dispose .
 *
 * @author elisontl
 */
public class DataFormatUtils {

    /**
     * Date -> Str ( yyyy-MM-dd HH:mm:ss )
     *
     * @param date
     * @return
     */
    public static String parseDate2FullCharacters(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

}
