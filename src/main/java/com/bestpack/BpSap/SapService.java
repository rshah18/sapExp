package com.bestpack.BpSap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

public class SapService {
    //static
    private static String SessionCookies; // holds session cookies
    //private
    private final String UserName = "rks";
    private final String Password = "1234";
    private final String CompanyDB = "BP_Live1";
    private HttpsURLConnection connection; //http connection
    private StringBuilder serviceUrl = new StringBuilder();
    private StringBuilder getfields = new StringBuilder();
    private StringBuilder orderBy = new StringBuilder();
    private StringBuilder filterArg = new StringBuilder();
    private String param = null;
    private int skipResults = 0;
    private int topResults = 20;
    //flags
    private boolean paramSet = false;
    private boolean selectSet = false;
    private boolean filterSet = false;
    private boolean orderBySet = false;
    private boolean skipSet = false;
    private boolean topSet = false;
    public boolean countSet = false;
    public boolean getHeader = false;
    private boolean requestParam = false;
    private int argNum = 0;
    private int selectNum = 0;
    // public
    public enum filter {
        CONTAINS("contains"),
        ENDSWITH("endswith"),
        STARTSWITH("startswith"),
        SUBSTRING("substringof");
        public final String value;
        filter(String _filterVal) {
            this.value = _filterVal;
        }
    }
    public enum RequestMethod {
        GET("GET"),
        POST("POST"),
        PATCH("PATCH"),
        DELETE("DELETE");
        public final String value;
        RequestMethod(String _value){
            this.value = _value;
        }
    }
    public final String  server ="https://192.168.88.22:50000/b1s/v1/";
    public RequestMethod requestMethod;
    public JsonObject requestBody = new JsonObject();


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

    public void filter(filter _filter, String arg, String val){
        paramSet = true;
        filterSet = true;
        filterArg.append(_filter.value);
        filterArg.append('(');
        filterArg.append(arg);
        filterArg.append(',');
        filterArg.append("%27");
        filterArg.append(val);
        filterArg.append("%27");
        filterArg.append(')');

    }

    public void select(String field){
        paramSet = true;
        selectSet = true;
        if(selectNum != 0){
            getfields.append(',');
        }
        getfields.append(field);
        selectNum++;
    }

    public void order(String field){
        paramSet = true;
        orderBySet = true;
        orderBy.append(field);
        orderBy.append(',');
    }
    public void getTop(int value){
        topResults = value;
        topSet = true;
    }
    public void skip(int value){
        skipResults = value;
        skipSet = true;
    }
    public void getCount(){
        paramSet = true;
        countSet = true;
    }
    public void setParam(String _param){
        requestParam = true;
        param = _param;
    }

    // public methods
    public SapService(RequestMethod _requestMethod, String _serviceUrl){
        requestMethod = _requestMethod;
        serviceUrl.append(_serviceUrl);
        getfields.append("$select=");
        filterArg.append("$filter=");
        orderBy.append("$orderby=");
    }

    public String getUrl(){
            if(requestParam){
                serviceUrl.append("(");
                serviceUrl.append(param);
                serviceUrl.append(")");
            }
            if(paramSet){
                serviceUrl.append("?");
                if(selectSet) {
                    if(argNum != 0){serviceUrl.append("&");}
                    serviceUrl.append(getfields.toString());
                    argNum++;
                }

                if(orderBySet){
                    if(argNum != 0){serviceUrl.append("&");}
                    serviceUrl.append(orderBy.toString());
                    argNum++;
                }
                if(filterSet) {
                    if(argNum != 0){serviceUrl.append("&");}
                    serviceUrl.append(filterArg.toString());
                    argNum++;
                }
                if(skipSet) {
                    if(argNum != 0){serviceUrl.append("&");}
                    serviceUrl.append("$skip=").append(Integer.toString(skipResults));
                    argNum++;
                }
                if(topSet) {
                    if(argNum != 0){serviceUrl.append("&");}
                    serviceUrl.append("$top=").append(Integer.toString(topResults));
                    argNum++;
                }
                if(countSet){
                    if(argNum != 0){serviceUrl.append("&");}
                    System.out.println("count is true ");
                    serviceUrl.deleteCharAt(serviceUrl.indexOf("?"));
                    serviceUrl.append("/$count");
                }
            }
            return serviceUrl.toString();

    }

    public JsonObject jsonStream(InputStream input){
        Reader reader = null;
        JsonObject result= null;
        try {
            reader = new InputStreamReader(input, "UTF-8");
            result  = new Gson().fromJson(reader, JsonObject.class);
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void setCookie(){
        // get the cookies
        System.out.println(requestBody.toString());
        System.out.println(process().toString());
        StringBuilder cookies = new StringBuilder();
        List<String> cookiesHeader = connection.getHeaderFields().get("Set-Cookie");
        if (cookiesHeader != null) for (String cookie : cookiesHeader) {
            HttpCookie unit = HttpCookie.parse(cookie).get(0);
            String unitCookie = unit.getName() + "=" + unit.getValue() + ";";
            cookies.append(unitCookie);
        }
        SessionCookies = cookies.toString();
        System.out.println("cookie:" + SessionCookies);
    }

    public JsonObject process(){
        try{
            URL url = new URL(server+ getUrl());
            System.out.println(url);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
            connection.setRequestMethod(requestMethod.value);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            // set cookies
            //System.out.println(SessionCookies == null);
            if(SessionCookies != null) connection.setRequestProperty("Cookie", SessionCookies);

            //connection.setRequestProperty("Cookie", "ROUTEID=.node3;B1SESSION=07681e5c-3e4f-11eb-8000-00505694e489");
            // send request
            if(requestBody.size() != 0){
                System.out.println("requestbody not null");
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            // get headers
            if(getHeader){
                connection.getHeaderFields().forEach((key, value) -> System.out.println(key + " : "+value));
            }

            // get result
            if(connection.getResponseCode() == 200){
                return jsonStream(connection.getInputStream());
            } else {
                return jsonStream(connection.getErrorStream());
            }

        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

}




