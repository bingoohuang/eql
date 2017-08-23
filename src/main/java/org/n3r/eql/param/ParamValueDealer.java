package org.n3r.eql.param;

import lombok.Getter;
import org.n3r.eql.param.EqlParamPlaceholder.Like;
import org.n3r.eql.util.S;

import java.sql.Timestamp;
import java.util.Date;

public class ParamValueDealer {
    private EqlParamPlaceholder placeHolder;
    @Getter Object boundParam;
    @Getter Object paramValue;

    public ParamValueDealer(EqlParamPlaceholder placeHolder) {
        this.placeHolder = placeHolder;
    }

    public void dealSingleValue(Object value) {
        boundParam = value;
        paramValue = value;

        if (value == null) return; // nothing to do

        if (value instanceof Date) {
            Timestamp date = new Timestamp(((Date) value).getTime());
            boundParam = S.toDateTimeStr(date);
            paramValue = date;
            return;
        }

        if (value.getClass().isEnum()) {
            if (value instanceof InternalValueable) {
                paramValue = ((InternalValueable) value).internalValue();
                boundParam = paramValue;
            }
            return;
        }

        if (placeHolder != null && value instanceof CharSequence) {
            String strValue = value.toString();
            if (placeHolder.isLob()) {
                paramValue = S.toBytes(strValue);
            } else if (placeHolder.getLike() == Like.Like) {
                paramValue = tryAddLeftAndRightPercent(strValue);
            } else if (placeHolder.getLike() == Like.RightLike) {
                paramValue = tryAddRightPercent(strValue);
            } else if (placeHolder.getLike() == Like.LeftLike) {
                paramValue = tryAddLeftPercent(strValue);
            } else if (placeHolder.isNumberColumn() && S.isBlank(strValue)) {
                paramValue = null;
            }
        }

        boundParam = paramValue;
    }

    private String tryAddLeftPercent(String strValue) {
        return addLeftPercent(strValue) + tryEscape(strValue);
    }

    private String tryAddRightPercent(String strValue) {
        return tryEscape(strValue) + addRightPercent(strValue);
    }

    private String tryAddLeftAndRightPercent(String strValue) {
        return addLeftPercent(strValue) + tryEscape(strValue) + addRightPercent(strValue);
    }

    private String addLeftPercent(String strValue) {
        return strValue.startsWith("%") ? (placeHolder.isEscape() ? "%" : "") : "%";
    }

    private String addRightPercent(String strValue) {
        return strValue.endsWith("%") ? (placeHolder.isEscape() ? "%" : "") : "%";
    }


    private String tryEscape(String strValue) {
        if (!placeHolder.isEscape()) return strValue;

        return strValue.replaceAll("[%_]", placeHolder.getEscapeValue() + "$0");
    }
}
