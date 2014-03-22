package tool.hc4;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.TrustStrategy;

/**
 * 不校验，统统认为是可信的
 * 
 * @author dhf
 */
public class TrustAnyStrategy implements TrustStrategy {

    private static final TrustAnyStrategy INSTANCE = new TrustAnyStrategy();

    public static TrustAnyStrategy get() {
        return INSTANCE;
    }

    private TrustAnyStrategy() {}

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        return true;
    }

}
