package org.n3r.eql.util;

import lombok.extern.slf4j.Slf4j;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;

@Slf4j
public class Og {
    public static Object eval(String expr, Map<String, Object> mergeProperties) {
        Exception ex = null;
        try {
            return Ognl.getValue(expr, new OgRoot(mergeProperties));
        } catch (NoSuchPropertyException e) { // ignore
        } catch (OgnlException e) {
            if (!e.getMessage().contains("source is null for getProperty"))
                ex = e;
        } catch (Exception e) {
            ex = e;
        }

        if (ex != null) log.warn("error while eval " + expr, ex);
        return null;
    }

}
