package org.n3r.eql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

public class HostAddress {
    private static Logger logger = LoggerFactory.getLogger(HostAddress.class);
    private static String ip;
    private static String host;

    static {
        NetworkInterface ni = null;

        try {
            ni = NetworkInterface.getByName("bond0");
        } catch (SocketException e) {
            logger.warn("Get NetworkInterface bond0 fail", e);
        }

        InetAddress inetAddress = null;
        if (null != ni) {
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress ia = addresses.nextElement();
                if (ia instanceof Inet6Address) continue;

                inetAddress = ia;
                ip = left(ia.getHostAddress(), 20);

                break;
            }
        } else {
            try {
                inetAddress = InetAddress.getLocalHost();
                ip = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                logger.warn("getHostAddress fail", e);
            }
        }

        if (inetAddress != null)

            host = left(inetAddress.getHostName(), 50);
    }

    public static String getHost() {
        return host;
    }

    public static String getIp() {
        return ip;
    }

    private static String left(String string, int len) {
        return string.length() <= len ? string : string.substring(0, len);
    }
}
