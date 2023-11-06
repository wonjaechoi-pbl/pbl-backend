package com.gogofnd.kb.global.config;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.*;
import java.security.cert.CertificateException;

// 레트로핏 때문에 있는거. 안건드려도 됨
public class OkHttpClients {
  public static OkHttpClient getUnsafeOkHttpClient(){

          try {
              final TrustManager[] trustAllCerts = new TrustManager[]{
                      new X509TrustManager() {
                          @Override
                          public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                          }

                          @Override
                          public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                          }

                          @Override
                          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                              return new java.security.cert.X509Certificate[]{};
                          }
                      }
              };

              // Install the all-trusting trust manager
              final SSLContext sslContext = SSLContext.getInstance("SSL");
              sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

              // Create an ssl socket factory with our all-trusting manager
              final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

              OkHttpClient.Builder builder = new OkHttpClient.Builder();
              builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
              builder.hostnameVerifier(new HostnameVerifier() {
                  @Override
                  public boolean verify(String hostname, SSLSession session) {
                      return true;
                  }
              });
              HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
              OkHttpClient okHttpClient = builder.addInterceptor(loggingInterceptor).build();
              return okHttpClient;
          } catch (Exception e) {
              throw new RuntimeException(e);
          }

      }

}
