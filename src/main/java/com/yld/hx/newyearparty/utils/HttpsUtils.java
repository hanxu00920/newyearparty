package com.yld.hx.newyearparty.utils;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class HttpsUtils {

	private static int maxTotal = 100;
	private static int defaultMaxPerRoute = 64;

	/**
	 * 
	 * @param maxTotal           连接池最大连接数
	 * @param defaultMaxPerRoute 每个route最大连接数
	 */
	public static void setConfig(int maxTotal, int defaultMaxPerRoute) {
		cm.setMaxTotal(maxTotal);// max connection
		cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
	}

	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private static SSLConnectionSocketFactory sslsf = null;
	private static PoolingHttpClientConnectionManager cm = null;
	private static SSLContextBuilder builder = null;
	static {
		try {
			builder = new SSLContextBuilder();
			// 全部信任 不做身份鉴定
			builder.loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			});
			sslsf = new SSLConnectionSocketFactory(builder.build(),
					new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register(HTTP, new PlainConnectionSocketFactory()).register(HTTPS, sslsf).build();
			cm = new PoolingHttpClientConnectionManager(registry);
			cm.setMaxTotal(maxTotal);// max connection
			cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * httpClient post请求
	 * 
	 * @param url    请求url
	 * @param header 头部信息
	 * @param param  请求参数 form提交适用
	 * @param entity 请求实体 json/xml提交适用
	 * @return 可能为空 需要处理
	 * @throws Exception
	 *
	 */
	public static String post(String url, Map<String, String> header, Map<String, String> param, HttpEntity entity)
			throws Exception {
		String result = "";
		CloseableHttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			// 设置头信息
			if(header!=null) {
				for (Map.Entry<String, String> entry : header.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if(param!=null) {
				// 设置请求参数
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : param.entrySet()) {
					// 给参数赋值
					formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
					UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
					httpPost.setEntity(urlEncodedFormEntity);
				}
			}
			
			// 设置实体 优先级高
			if (entity != null) {
				httpPost.setEntity(entity);
			}

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
					.setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
			httpPost.setConfig(requestConfig);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = httpResponse.getEntity();
				result = EntityUtils.toString(resEntity);
			} else {
				readHttpResponse(httpResponse);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
//                httpClient.close();
			}
		}
		return result;
	}

	public static String get(String url) throws Exception {
		String result = "";
		CloseableHttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpGet httpGet = new HttpGet(url);

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
					.setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
			httpGet.setConfig(requestConfig);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = httpResponse.getEntity();
				result = EntityUtils.toString(resEntity);
			} else {
				readHttpResponse(httpResponse);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
                httpClient.close();
			}
		}
		return result;

	}
	
	public static String getByProxy(String url, String proxyHost, int proxyPort) throws Exception {
		String result = "";
		CloseableHttpClient httpClient = null;
		try {
			httpClient = getProxyHttpClient(proxyHost, proxyPort);
			HttpGet httpGet = new HttpGet(url);

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
					.setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
			httpGet.setConfig(requestConfig);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = httpResponse.getEntity();
				result = EntityUtils.toString(resEntity);
			} else {
				readHttpResponse(httpResponse);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
                httpClient.close();
			}
		}
		return result;

	}

	public static CloseableHttpClient getHttpClient() throws Exception {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm)
				.setConnectionManagerShared(true).build();
		return httpClient;
	}
	
	public static CloseableHttpClient getProxyHttpClient(String proxyHost, int proxyPort) throws Exception {
		CloseableHttpClient httpClient = HttpClients.custom().setProxy(new HttpHost(proxyHost, proxyPort)).setSSLSocketFactory(sslsf).setConnectionManager(cm)
				.setConnectionManagerShared(true).build();
		return httpClient;
	}

	public static String readHttpResponse(HttpResponse httpResponse) throws ParseException, IOException {
		StringBuilder builder = new StringBuilder();
		// 获取响应消息实体
		HttpEntity entity = httpResponse.getEntity();
		// 响应状态
		builder.append("status:" + httpResponse.getStatusLine());
		builder.append("headers:");
		HeaderIterator iterator = httpResponse.headerIterator();
		while (iterator.hasNext()) {
			builder.append("\t" + iterator.next());
		}
		// 判断响应实体是否为空
		if (entity != null) {
			String responseString = EntityUtils.toString(entity);
			builder.append("response length:" + responseString.length());
			builder.append("response content:" + responseString.replace("\r\n", ""));
		}
		return builder.toString();
	}

	public static void main(String[] args) {
//		StringEntity entity = new StringEntity("haha", "utf-8");
//		try {
//			System.out.println(new Date());
//			String post = HttpsUtils.post("http://192.168.101.128:9080/payment/test", new HashMap<String, String>(),
//					new HashMap<String, String>(), entity);
//			System.out.println(post);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println(new Date());
		try {
			String byProxy = HttpsUtils.getByProxy("https://124.42.103.156:8089/maap/", "123.57.154.30", 8901);
			System.out.println(byProxy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
