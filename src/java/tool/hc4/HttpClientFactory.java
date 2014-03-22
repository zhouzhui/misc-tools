package tool.hc4;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 用于创建HttpClient，非线程安全。依赖httpclient-4.3
 * 
 * @author dhf
 */
@NotThreadSafe
public class HttpClientFactory {
    private static final char[] KEY_STORE_PASSWORD = "changeit".toCharArray();

    private int requestTimeout = 5000;

    private int connectTimeout = 5000;

    private boolean allowCircularRedirect = false;

    private SSLContext sslContext;

    /**
     * 根据已设定的参数，生成httpclient。请查看所有setter方法的注释以了解默认值
     */
    public CloseableHttpClient create() {
        RequestConfig reqConf = RequestConfig.custom()
                .setCircularRedirectsAllowed(allowCircularRedirect)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .setSocketTimeout(requestTimeout).build();

        if (null == sslContext) {
            try {
                sslContext = SSLContexts.custom()
                        .loadTrustMaterial(null, TrustAnyStrategy.get())
                        .build();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        return HttpClients.custom().disableAuthCaching()
                .disableAutomaticRetries().disableCookieManagement()
                .setDefaultRequestConfig(reqConf).setSslcontext(sslContext)
                .build();
    }

    /**
     * 是否允许循环重定向。默认不允许
     * 
     * @param allowCircularRedirect
     */
    public void setAllowCircularRedirect(boolean allowCircularRedirect) {
        this.allowCircularRedirect = allowCircularRedirect;
    }

    /**
     * http连接超时，单位毫秒。默认5000毫秒
     * 
     * @param connectTimeout
     */
    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = (int) connectTimeout;
    }

    /**
     * http请求超时时间，单位毫秒。默认5000毫秒
     * 
     * @param requestTimeout
     */
    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = (int) requestTimeout;
    }

    /**
     * 设置SSL连接的上下文（证书校验）。默认不校验，信任所有证书
     * 
     * @param sslContext
     */
    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * 设置可信SSL证书的keystore
     * 
     * @param keystore
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public void setKeystore(KeyStore keystore) throws KeyManagementException,
            NoSuchAlgorithmException, KeyStoreException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(keystore).build();
        setSslContext(sslContext);
    }

    /**
     * 设置可信SSL证书的keystore
     * 
     * @param keyStoreIn
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyManagementException
     */
    public void setKeystore(InputStream keyStoreIn) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException,
            KeyManagementException {
        setKeystore(keyStoreIn, null);
    }

    /**
     * 设置可信SSL证书的keystore
     * 
     * @param keyStoreIn
     * @param password
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyManagementException
     */
    public void setKeystore(InputStream keyStoreIn, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (null == password) {
            password = KEY_STORE_PASSWORD;
        }
        keyStore.load(keyStoreIn, password);
        setKeystore(keyStore);
    }

}
