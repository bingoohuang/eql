package org.n3r.eql.util;


import lombok.Value;
import lombok.val;
import ognl.MemberAccess;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * This class provides methods for setting up and restoring
 * access in a Field.  Java 2 provides access utilities for setting
 * and getting fields that are non-public.  This object provides
 * coarse-grained access controls to allow access to private, protected
 * and package protected members.  This will apply to all classes
 * and members.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 * @version 15 October 1999
 */
@Value
public class DefaultMemberAccess implements MemberAccess {
    private final boolean allowPrivateAccess;
    private final boolean allowProtectedAccess;
    private final boolean allowPackageProtectedAccess;

    /*===================================================================
        Constructors
      ===================================================================*/
    public DefaultMemberAccess(boolean allowAllAccess) {
        this(allowAllAccess, allowAllAccess, allowAllAccess);
    }

    public DefaultMemberAccess(boolean allowPrivateAccess, boolean allowProtectedAccess, boolean allowPackageProtectedAccess) {
        super();
        this.allowPrivateAccess = allowPrivateAccess;
        this.allowProtectedAccess = allowProtectedAccess;
        this.allowPackageProtectedAccess = allowPackageProtectedAccess;
    }


    /*===================================================================
        MemberAccess interface
      ===================================================================*/
    public Object setup(Map context, Object target, Member member, String propertyName) {
        Object result = null;

        if (isAccessible(context, target, member, propertyName)) {
            val accessible = (AccessibleObject) member;

            if (!accessible.isAccessible()) {
                result = Boolean.FALSE;
                accessible.setAccessible(true);
            }
        }
        return result;
    }

    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
        if (state != null) {
            val accessible = (AccessibleObject) member;
            val stateboolean = (Boolean) state;  // Using twice (avoid unboxing)
            if (!stateboolean) {
                accessible.setAccessible(false);
            } else {
                throw new IllegalArgumentException("Improper restore state [" + stateboolean + "] for target [" + target +
                        "], member [" + member + "], propertyName [" + propertyName + "]");
            }
        }
    }

    /**
     * Returns true if the given member is accessible or can be made accessible
     * by this object.
     */
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        int modifiers = member.getModifiers();
        boolean result = Modifier.isPublic(modifiers);

        if (!result) {
            if (Modifier.isPrivate(modifiers)) {
                result = isAllowPrivateAccess();
            } else {
                if (Modifier.isProtected(modifiers)) {
                    result = isAllowProtectedAccess();
                } else {
                    result = isAllowPackageProtectedAccess();
                }
            }
        }
        return result;
    }
}