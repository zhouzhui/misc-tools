package tool.hc4;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 用于向http server发出请求并读取返回的内容
 * 
 * @author dhf
 */
@ThreadSafe
public class HttpRequester {
    private static final String DEF_CHARSET = "utf-8";

    private CloseableHttpClient client;

    public HttpRequester() {
        this(new HttpClientFactory().create());
    }

    public HttpRequester(CloseableHttpClient client) {
        this.client = client;
    }

    /**
     * 发出http get请求，读取http server返回的content
     * 
     * @param uri
     * @return
     * @throws IOException
     */
    public byte[] get(String uri) throws IOException {
        return get(uri, null, null);
    }

    /**
     * 发出http get请求，读取http server返回的content。参数以指定的编码处理
     * 
     * @param uri
     * @param parameters
     *            无参数时可为null
     * @param defaultCharset
     *            请求参数的默认编码；若为null则默认utf-8
     * @return
     * @throws IOException
     */
    public byte[] get(String uri, List<NameValuePair> parameters,
            String defaultCharset) throws IOException {
        HttpGet httpRequest = makeGetRequest(uri, parameters, defaultCharset);
        return request(httpRequest);
    }

    /**
     * 发出http get请求，读取http server返回的content并解析成字符串。当http
     * server未返回Content-Type或Content-Type中未包含编码时当做utf-8编码处理
     * 
     * @param uri
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String getAndParse(String uri) throws ParseException, IOException {
        return getAndParse(uri, null, null);
    }

    /**
     * 发出http get请求，读取http server返回的content并解析成字符串
     * 
     * @param uri
     * @param parameters
     *            无参数时可为null
     * @param defaultCharset
     *            请求参数及响应内容的默认编码；若为null则默认utf-8
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String getAndParse(String uri, List<NameValuePair> parameters,
            String defaultCharset) throws ParseException, IOException {
        if (null == defaultCharset) {
            defaultCharset = DEF_CHARSET;
        }
        HttpGet httpRequest = makeGetRequest(uri, parameters, defaultCharset);
        return requestAndParse(httpRequest, defaultCharset);
    }

    /**
     * 发出http post请求，读取http server返回的content。参数以指定的编码处理
     * 
     * @param uri
     * @param parameters
     *            无参数时可为null
     * @param defaultCharset
     *            请求参数的默认编码；若为null则默认utf-8
     * @return
     * @throws IOException
     */
    public byte[] post(String uri, List<NameValuePair> parameters,
            String defaultCharset) throws IOException {
        HttpPost httpRequest = makePostRequest(uri, parameters, defaultCharset);
        return request(httpRequest);
    }

    /**
     * 发出http get请求，读取http server返回的content并解析成字符串
     * 
     * @param uri
     * @param parameters
     *            无参数时可为null
     * @param defaultCharset
     *            请求参数及响应内容的默认编码；若为null则默认utf-8
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String postAndParse(String uri, List<NameValuePair> parameters,
            String defaultCharset) throws ParseException, IOException {
        if (null == defaultCharset) {
            defaultCharset = DEF_CHARSET;
        }
        HttpPost httpRequest = makePostRequest(uri, parameters, defaultCharset);
        return requestAndParse(httpRequest, defaultCharset);
    }

    private HttpGet makeGetRequest(String uri, List<NameValuePair> parameters,
            String charset) {
        String queryString = null;
        if (null != parameters && parameters.size() > 0) {
            if (null == charset) {
                charset = DEF_CHARSET;
            }
            queryString = URLEncodedUtils.format(parameters, charset);
        }
        String requestUri = uri;
        if (null != queryString) {
            if (-1 == uri.indexOf("?")) {
                requestUri = uri + "?" + queryString;
            } else {
                requestUri = uri + "&" + queryString;
            }
        }
        return new HttpGet(requestUri);
    }

    private HttpPost makePostRequest(String uri,
            List<NameValuePair> parameters, String charset) throws IOException {
        HttpPost post = new HttpPost(uri);
        if (null != parameters && parameters.size() > 0) {
            if (null == charset) {
                charset = DEF_CHARSET;
            }
            UrlEncodedFormEntity urfe = new UrlEncodedFormEntity(parameters,
                    charset);
            post.setEntity(urfe);
        }
        return post;
    }

    /**
     * 发出http请求，读取http server返回的content
     * 
     * @param httpRequest
     * @return
     * @throws IOException
     */
    public byte[] request(HttpUriRequest httpRequest) throws IOException {
        HttpResponse httpResponse = client.execute(httpRequest);
        HttpEntity entity = httpResponse.getEntity();
        if (null == entity) {
            return null;
        }
        return EntityUtils.toByteArray(entity);
    }

    /**
     * 发出http请求，读取http server返回的content并解析成字符串。当http
     * server未返回Content-Type或Content-Type中未包含编码时当做utf-8编码处理
     * 
     * @param httpRequest
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String requestAndParse(HttpUriRequest httpRequest)
            throws ParseException, IOException {
        return requestAndParse(httpRequest, DEF_CHARSET);
    }

    /**
     * 发出http请求，读取http server返回的content并解析成字符串
     * 
     * @param httpRequest
     * @param defaultCharset
     *            当http server未返回Content-Type或Content-Type中未包含编码时使用的默认编码
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String requestAndParse(HttpUriRequest httpRequest,
            String defaultCharset) throws ParseException, IOException {
        HttpResponse httpResponse = client.execute(httpRequest);
        HttpEntity entity = httpResponse.getEntity();
        if (null == entity) {
            return null;
        }
        return EntityUtils.toString(entity, defaultCharset);
    }

    /**
     * 释放所有资源
     * 
     * @throws IOException
     */
    public void destroy() throws IOException {
        if (null != client) {
            client.close();
        }
    }
}
