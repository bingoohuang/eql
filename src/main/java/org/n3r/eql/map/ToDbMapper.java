package org.n3r.eql.map;

/**
 * Mapping business object to jdbc compatible object.
 * like joda DateTime/LocalDate/LocalTime to jdbc TimeStamp/Date/Time。
 */
public interface ToDbMapper {
    /**
     * If The object supported by current mapper.
     *
     * @param clazz business class to be mapped.
     * @return mapped jdbc object.
     */
    boolean support(Class<?> clazz);

    Object map(Object obj);
}
