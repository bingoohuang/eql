package org.n3r.eql.util;

import java.io.Closeable;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;

public class HostAddress {
    private static String ip;
    private static String host;

    static {
        StringBuilder ips = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback()) continue;

                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        ips.append(inetAddress.getHostAddress()).append(",");
                    }
                }

            }
        } catch (SocketException e) {
            // ignore
        }

        if (ips.length() == 0) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ip = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                // ignore
            }
        } else {
            ip = ips.deleteCharAt(ips.length() - 1).toString();
        }

        host = hostname();
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

    public static void main(String[] args) {
        System.out.println(HostAddress.getIp());
        System.out.println(HostAddress.getHost());
    }

    public static String hostname() {
        Process proc = null;
        InputStream stream = null;
        Scanner s = null;
        try {
            proc = Runtime.getRuntime().exec("hostname");
            stream = proc.getInputStream();
            s = new Scanner(stream).useDelimiter("\\A");
            return s.hasNext() ? s.next().trim() : "";
        } catch (Exception e) {
            return "unknown";
        } finally {
            closeQuietly(stream);
            closeQuietly(s);
            if (proc != null) proc.destroy();
            if (s != null) s.close();
        }
    }

    public static void closeQuietly(Closeable stream) {
        if (stream != null) try {
            stream.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
