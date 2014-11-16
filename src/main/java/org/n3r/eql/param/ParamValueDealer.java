package org.n3r.eql.param;

import org.n3r.eql.util.S;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ParamValueDealer {
    private EqlParamPlaceholder placeHolder;
    private  Object boundParam;
    private Object paramValue;

    public ParamValueDealer(EqlParamPlaceholder placeHolder) {
        this.placeHolder = placeHolder;
    }

    public Object getBoundParam() {
        return boundParam;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public void dealSingleValue(Object value) {
        boundParam = value;
        paramValue = value;

        if (value == null) {
            // nothing to do
        } else if (value instanceof Date) {
            Timestamp date = new Timestamp(((Date) value).getTime());
            boundParam = S.toDateTimeStr(date);
            paramValue = date;
        } else if (value.getClass().isEnum()) {
            if (value instanceof InternalValueable) {
                paramValue = ((InternalValueable) value).internalValue();
                boundParam = paramValue;
            }
        } else {
            if (placeHolder != null && value instanceof CharSequence) {
                String strValue = value.toString();
                if (placeHolder.isLob()) {
                    paramValue = S.toBytes(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.Like) {
                    paramValue = tryAddLeftAndRightPercent(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.RightLike) {
                    paramValue = tryAddRightPercent(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.LeftLike) {
                    paramValue = tryAddLeftPercent(strValue);
                } else if (placeHolder.isNumberColumn() && strValue.trim().length() == 0) {
                    paramValue = null;
                }
            }

            boundParam = paramValue;
        }
    }

    private String tryAddLeftPercent(String strValue) {
        return (strValue.startsWith("%") ? "" : "%") + strValue;
    }

    private String tryAddRightPercent(String strValue) {
        return strValue + (strValue.endsWith("%") ? "" : "%");
    }

    private String tryAddLeftAndRightPercent(String strValue) {
        return (strValue.startsWith("%") ? "" : "%") + strValue + (strValue.endsWith("%") ? "" : "%");
    }
}
