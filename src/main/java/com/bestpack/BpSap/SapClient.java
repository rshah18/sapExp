package com.bestpack.BpSap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SapClient {
    public static void main(String[] arg){
        // login
        SapService login = new SapService(SapService.RequestMethod.POST, "Login");
        login.requestBody.addProperty("CompanyDB", "BP_Live1");
        login.requestBody.addProperty("UserName", "rks");
        login.requestBody.addProperty("Password", "1234");
        login.setCookie();

        SapService items = new SapService(SapService.RequestMethod.GET, "Items");
        items.getHeader = true;
        items.filter(SapService.filter.CONTAINS, "ItemCode","NT00480" );
        items.select("ItemWarehouseInfoCollection");
        items.select("ItemCode");
        items.select("ItemName");
        items.select("QuantityOnStock");
        JsonObject result = items.process();//

        if(result.get("value").getAsJsonArray().size()== 0){
            System.out.println("Invalid part number!");
        }
        else {
            JsonObject value = result.get("value").getAsJsonArray().get(0).getAsJsonObject();
            JsonElement binLocation = value.get("ItemWarehouseInfoCollection").getAsJsonArray().get(0).getAsJsonObject().get("U_BinLoc");
            String code = value.get("ItemCode").getAsString();
            String name = value.get("ItemName").getAsString();
            String stock = value.get("QuantityOnStock").getAsString();
            if(binLocation != null){

            }
            System.out.println("binLocation" + " "+  code +  " " + name + " " + stock);
        }










    }
}
