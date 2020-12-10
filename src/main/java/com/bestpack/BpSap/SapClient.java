package com.bestpack.BpSap;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import net.minidev.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SapClient {
    static String  serverUrl ="https://192.168.88.22:50000/b1s/v1/";
    static final String COOKIES_HEADER = "Set-Cookie";
    static String part = "NT00480";
    //Login,
    static String  service = "Items?$select=ItemCode,ItemName,QuantityOnStock,&$filter=startswith(ItemCode,%27"+ part +"%27)";
    static URL url;


    public static void printStream(InputStream input){
        String text = null;
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        System.out.println(text);
    }

    public static void main(String[] arg){

        //Trust manager
        TrustManager[] trustAllCerts = new TrustManager[]{
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


        try {

            // ssl
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);

            //json
            JSONObject json = new JSONObject();
            json.appendField("CompanyDB", "BP_Live1");
            json.appendField("UserName", "rks");
            json.appendField("Password", "1234");
            String cooky = "ROUTEID=.node9"+";" +"B1SESSION=0439fd90-3a76-11eb-8000-00505694e489";

            url = new URL(serverUrl+service);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", cooky);
            connection.setUseCaches(true);
            System.out.println(connection.getURL().toString());

            /*
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

             */

            // print header
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());
            connection.getHeaderFields().forEach((key, value) -> System.out.println(key + " : "+value));
            //cookies
            CookieManager msCookieManager = new CookieManager();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    HttpCookie unit = HttpCookie.parse(cookie).get(0);
                    System.out.println(unit.getName() + ":" + unit.getValue());
                    msCookieManager.getCookieStore().add(null, unit);

                }
            }


            if(connection.getResponseCode() == 200){
                printStream(connection.getInputStream());
            } else {
                printStream(connection.getErrorStream());
            }
            //





        } catch (KeyManagementException | IOException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }


    }
}
