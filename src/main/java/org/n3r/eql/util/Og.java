package org.n3r.eql.util;

import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Og {
    static Logger log = LoggerFactory.getLogger(Og.class);

    public static Object eval(String expr, Map<String, Object> mergeProperties) {
        Exception ex = null;
        try {
            return Ognl.getValue(expr, new OgRoot(mergeProperties));
        } catch (NoSuchPropertyException e) { // ignore
        } catch (OgnlException e) {
            if (!e.getMessage().contains("source is null for getProperty")) ex = e;
        } catch (Exception e) {
            ex = e;
        }

        if (ex != null) log.warn("error while eval " + expr, ex);
        return null;
    }

}
