package com.bestpack.BpSap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import net.minidev.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpCookie;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class SapClient {
    //global constants
    private static final String UserName = "rks";
    private static final String Password = "1234";
    private static final String CompanyDB = "BP_Live1";

    private static String SessionCookies = null; // holds session cookies
    public static String  serverUrl ="https://192.168.88.22:50000/b1s/v1/";
    private static  HttpsURLConnection connection; //http connection

    //printStream
    public void getStream(InputStream input){
        String text = null;
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        System.out.println(text);

    }
    public JsonObject printStream(InputStream input){
        Reader reader = null;
        JsonObject result= null;
        try {
            reader = new InputStreamReader(input, "UTF-8");
            result  = new Gson().fromJson(reader, JsonObject.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;

    }

    //Trust manager
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

    private SSLContext sslContext(){
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, getTrust(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return sc;
    }

    public JsonObject processRequest(String requestMethod, String serviceUrl, String requestBody){
        try {
            URL url = new URL(serverUrl+ serviceUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
            connection.setRequestMethod(requestMethod);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            if(SessionCookies !=null){
                connection.setRequestProperty("Cookie", SessionCookies);
            } else {
                // temp
                String localCookie = "B1SESSION=1172dfda-3be4-11eb-8000-00505694e489;";
                connection.setRequestProperty("Cookie", localCookie);
            }

            //process requestbody
            if(requestBody!=null){
                System.out.println("requestbody not null");
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // headers
            connection.getHeaderFields().forEach((key, value) -> System.out.println(key + " : "+value));

            if(connection.getResponseCode() == 200){
                //getStream(connection.getInputStream());
                return printStream(connection.getInputStream());
            } else {
                //getStream(connection.getErrorStream());
                return printStream(connection.getErrorStream());
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    //login function
    public void login(){
        // service url
        String serviceUrl = "Login";
        // credentials
        JSONObject credentials = new JSONObject();
        credentials.appendField("CompanyDB", CompanyDB);
        credentials.appendField("UserName", UserName);
        credentials.appendField("Password", Password);
        // processLogin
        JsonObject result = processRequest("POST", "Login", credentials.toString());
        System.out.println(result);
        // get the cookies
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

    public static String quantity(String part){
        //StringBuilder select = null;
        String select = "he";
        String serviceUrl = "Items?$select="+ select+ "&$filter=startswith(ItemCode,%27" + part+ "%27)";
        String serviceUrl3 = "Items?$select=QuantityOnStock&$filter=startswith(ItemCode,%27" + part+ "%27)";
        String serviceUrl2 = "Items?$filter=startswith(ItemCode,%27" + part+ "%27)";
         JsonArray quantity = (JsonArray) Objects.requireNonNull(processRequest("GET", serviceUrl3, null)).get("value");
         return quantity.get(0).getAsJsonObject().get("QuantityOnStock").toString();

    }

    public static void invoices(String invoice){

        String serviceUrl = "Invoices?$filter=startswith(DocNum,%27" + invoice+ "%27)";
        processRequest("GET", serviceUrl, null);
        //System.out.println(connection.getURL());
    }


    public static void main(String[] arg){
       //login();
        invoices("138250");

    }
}
