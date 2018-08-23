package org.n3r.eql.map;

import java.sql.SQLException;

/**
 * Mapping jdbc compatible object to business object.
 * like jdbc TimeStamp/Date/Time to joda DateTime/LocalDate/LocalTime。
 */
public interface FromDbMapper {
    /**
     * If The object supported by current mapper.
     *
     * @param clazz business class to be mapped.
     * @return mapped jdbc object.
     */
    boolean support(Class<?> clazz);


    Object map(RsAware rs, int index) throws SQLException;
}
