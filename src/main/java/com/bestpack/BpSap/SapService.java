package com.bestpack.BpSap;

import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class SapService {
    private final String UserName = "rks";
    private final String Password = "1234";
    private final String CompanyDB = "BP_Live1";
    private HttpsURLConnection connection; //http connection
    private String SessionCookies = null; // holds session cookies
    // public
    public String  serverUrl ="https://192.168.88.22:50000/b1s/v1/";
    public String requestMethod;
    public String serviceUrl;
    public String requestBody;
    public StringBuilder getfields = null;
    public StringBuilder orderBy = null;
    public StringBuilder filterArg = null;
    public int skipResults = 0;
    public int topResults = 20;
    private enum filter{
        STARTSWITH("startswith"),
        ENDSWITH("endswith"),
        CONTAINS("contains"),
        SUBSTRING("substringof");
        public final String value;
        filter(String _filterVal){
            this.value = _filterVal;
        }
    }



    // Trust manager
    private TrustManager[] getTrust(){
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    // SSLContext
    private SSLContext sslContext(){
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, getTrust(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println(e.getMessage());
        }
        return sc;
    }

    public void filter(filter _filter, String arg){
        filterArg.append(_filter.value);
        filterArg.append("")
    }
    // public methods
    public SapService(String _requestMethod, String _serviceUrl){
        requestMethod = _requestMethod;
        serverUrl = _serviceUrl;
    }

    }




