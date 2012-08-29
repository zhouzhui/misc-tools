package tool.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class IPv4Util {
    private static final Set<String> privateIPSet = new HashSet<String>();
    static {
        // Private IPv4 addresses as per RFC 1918
        addIPAddrs(privateIPSet, "10.0.0.0/8");
        addIPAddrs(privateIPSet, "172.16.0.0/12");
        addIPAddrs(privateIPSet, "192.168.0.0/16");
        // Automatic Private IP addresses reserved by IANA.
        addIPAddrs(privateIPSet, "169.254.0.0/16");
        // Loopback IP addresses
        addIPAddrs(privateIPSet, "127.0.0.1/8");
    }

    /**
     * @param ipset
     *            包含IP地址或IP段的集合
     * @param ip
     *            要加入到集合中的IP地址或CIDR表示法的IP段
     */
    public static void addIPAddrs(Collection<String> ipset, String ip) {
        int index = -1;
        if ((index = ip.indexOf("/")) == -1) {
            ipset.add(getBinaryIP(ip));
        } else {
            String ipHead = ip.substring(0, index);
            String ipTail = ip.substring(index + 1);
            try {
                int cidrBlock = Integer.parseInt(ipTail);
                if (cidrBlock < 1 || cidrBlock > 30) {
                    return;
                }
                ipset.add(getBinaryIP(fillIP(ipHead)).substring(0, cidrBlock));
            } catch (Exception e) {}
        }
    }

    /**
     * 判断给定的 <code>ip</code> 是否在 <code>ipranges</code> 范围内。
     * 
     * @param ipranges
     *            IP地址或CIDR表示法的IP段的集合
     * @param ip
     *            需要判断的IP地址
     * @return 若 <code>ip</code> 不是一个有效的IP地址，返回false；若 <code>ip</code> 不在
     *         <code>ipranges</code> 表示的范围内，返回false； 其他返回true
     */
    public static boolean isInIPAddrRange(Set<String> ipranges, String ip) {
        if (!isValidIP(ip)) {
            return false;
        }
        boolean flag = false;
        Set<String> ranges = ipranges;
        Iterator<String> iter = ranges.iterator();
        while (iter.hasNext()) {
            String range = (String) iter.next();
            if (getBinaryIP(ip).startsWith(range)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 获取用户的IP地址（最接近用户的外网IP地址）
     * 
     * @param request
     * @return
     */
    public static String getRequestIP(HttpServletRequest request) {
        String[] headers = new String[] {
            "x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };

        String ip = getRequestIP(request, headers, false);
        if ("".equals(ip)) {
            ip = getRequestIP(request, headers, true);
        }
        if ("".equals(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip.trim();
    }

    private static String getRequestIP(HttpServletRequest request,
            String[] headers, boolean allowPrivateIP) {
        for (String header: headers) {
            String ipListStr = request.getHeader(header);
            String ip = parseIP(ipListStr, allowPrivateIP);
            if (!"".equals(ip)) {
                return ip;
            }
        }
        return "";
    }

    /**
     * 从包含N个代理服务器地址的IP字符串中找出真实用户的ip,ipListStr里每个IP地址间以英文逗号","隔开
     * 
     * @param ipListStr
     * @param allowPrivateIP
     * @return
     */
    private static String parseIP(String ipListStr, boolean allowPrivateIP) {
        if (null == ipListStr) {
            return "";
        }
        String[] ips = ipListStr.split(",");
        String result = "";
        for (int i = ips.length - 1; i >= 0; i--) {
            String ip = ips[i].trim();
            // 非有效ip地址，继续检查下一个
            if (!isValidIP(ip)) {
                continue;
            }
            // 私有ip地址，继续检查下一个
            if (!allowPrivateIP && isInIPAddrRange(privateIPSet, ip)) {
                continue;
            }
            result = ip;
            break;
        }
        return result;
    }

    /**
     * 若ip为完整ip地址，则返回ip本身；若ip为不完整ip地址，如172.18.60，则填充为172.18.60.0
     * 
     * @param ip
     * @return
     */
    public static String fillIP(String ip) {
        if (null == ip) {
            return null;
        }
        String rs = ip.trim();
        String[] ips = rs.split("[.]");
        if (ips.length != 4) {
            int len = ips.length;
            for (int i = 0; i < 4 - len; i++) {
                rs += ".0";
            }
        }
        return rs;
    }

    /**
     * 判断是否是有效的ip地址（包括广播地址、网络）
     * 
     * @param ip
     * @return
     */
    public static boolean isValidIP(String ip) {
        if (null == ip) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (4 != parts.length) {
            return false;
        }
        for (String part: parts) {
            try {
                int n = Integer.parseInt(part);
                if ((n > 255) || (n < 0)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将IP地址转换为2进制字符串（32位）
     * 
     * @param ip
     * @return
     */
    public static String getBinaryIP(String ip) {
        if (!isValidIP(ip)) {
            return null;
        }
        String[] arr = ip.split("\\.");
        try {
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < arr.length; i++) {
                sb.append(toBinaryString(Integer.parseInt(arr[i]), 8));
            }
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private static String toBinaryString(int integer, int minLength) {
        String bstr = Integer.toBinaryString(integer);
        while (bstr.length() < minLength) {
            bstr = "0" + bstr;
        }
        return bstr;
    }
}
