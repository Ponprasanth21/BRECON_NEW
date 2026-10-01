package com.bornfire.recon.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
@Configuration
public class MPayConfig {
//	@Bean
//	public RestTemplate restTemplate() throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {
//		KeyStore ks = KeyStore.getInstance("JKS");
//		char[] pwdArray = "_password_".toCharArray();
//
//		ks.load(new FileInputStream(ResourceUtils.getFile("classpath:bob.jks")), pwdArray);
//		
//		SSLContext sslContext=org.apache.http.ssl.SSLContextBuilder.create()
//				.loadKeyMaterial(ks, pwdArray)
//				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
//				.build();
//		
//		SSLConnectionSocketFactory socketFactory=new SSLConnectionSocketFactory(sslContext,NoopHostnameVerifier.INSTANCE);
//		
//		//HttpClient httpClient=HttpClients.custom().setSSLContext(sslContext).build();
//		HttpClient httpClient=HttpClients.custom().setSSLSocketFactory(socketFactory).build();
//	
//		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
//				httpClient);
//		
//	90310190014370
//		RestTemplate restTemplate = new RestTemplate(requestFactory);
//		return restTemplate;
//		//return builder.errorHandler(getRestErrorHandler()).build();
//	}
	

	
	
//	@Bean
//	public RestTemplate restTemplate() {
//		
//		return new RestTemplate();
//	}
	
	
	 @Bean
	    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyManagementException {
	        // Create TrustManager that accepts all certificates
	        TrustManager[] trustAllCertificates = new TrustManager[]{
	            new X509TrustManager() {
	                public X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }

	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }

	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	        };

	        // Install the all-trusting TrustManager
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

	        // Create an HttpClient with the SSLContext that ignores certificate verification
	        HttpClient httpClient = HttpClients.custom()
	                .setSslcontext(sslContext)
	                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)  // Disable hostname verification
	                .build();

	        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

	        // Return RestTemplate using the custom HttpClient
	        return new RestTemplate(factory);
	    }
}
