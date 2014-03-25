package org.n3r.eql.param;

import com.google.common.base.Splitter;
import org.n3r.eql.util.EqlUtils;

public class EqlParamPlaceholder {
    private boolean lob;
    private Like like = Like.None;

    public static enum InOut {
        IN, OUT, INOUT
    }

    public static enum Like {
        None, Like, LeftLike, RightLike
    }

    private String placeholder;
    private InOut inOut = InOut.IN;
    private PlaceholderType placeholderType;
    private int seq;
    private String option;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public PlaceholderType getPlaceholderType() {
        return placeholderType;
    }

    public void setPlaceholderType(PlaceholderType placeholderType) {
        this.placeholderType = placeholderType;
    }

    public InOut getInOut() {
        return inOut;
    }

    public void setInOut(InOut inOut) {
        this.inOut = inOut;
    }

    public void setOption(String placeHolderOption) {
        this.option = placeHolderOption;
        Iterable<String> optionParts = Splitter.on(',').omitEmptyStrings().trimResults().split(this.option);
        for (String optionPart : optionParts) {
            if (EqlUtils.equalsIgnoreCase("OUT", optionPart)) setInOut(InOut.OUT);
            else if (EqlUtils.equalsIgnoreCase("INOUT", optionPart)) setInOut(InOut.INOUT);
            else if (EqlUtils.equalsIgnoreCase("LOB", optionPart)) setLob(true);
            else if (EqlUtils.equalsIgnoreCase("Like", optionPart)) setLike(Like.Like);
            else if (EqlUtils.equalsIgnoreCase("LeftLike", optionPart)) setLike(Like.LeftLike);
            else if (EqlUtils.equalsIgnoreCase("RightLike", optionPart)) setLike(Like.RightLike);
        }
    }

    public void setLob(boolean lob) {
        this.lob = lob;
    }

    public boolean isLob() {
        return lob;
    }

    public void setLike(Like like) {
        this.like = like;
    }

    public Like getLike() {
        return like;
    }
}
