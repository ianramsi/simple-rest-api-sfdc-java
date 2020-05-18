package sfdc_rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

public class Main {


    static String grantService = "/services/oauth2/token?grant_type=password";
    private static String baseUri;
    private static Header oauthHeader;
    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint","1");
    private static String restEndPoint = "/services/data/v48.0";
    private static String accountId;
    private static String accountName;
    private static String accountTenantId;


    public static void main (String [] args){

        String urlLogin = Credential.getUrlLogin();
        String usrName = Credential.getUserName();
        String pwd = Credential.getPassword();
        String clSec = Credential.getClientSecret();
        String clKey = Credential.getClientKey();

        HttpClient httpClient = HttpClientBuilder.create().build();
        String loginUrl = urlLogin + grantService + "&client_id=" + clSec + "&client_secret=" + clKey + "&username=" + usrName + "&password=" + pwd;
        System.out.println("login url --> "+loginUrl);

        HttpPost httpPost = new HttpPost(loginUrl);
        HttpResponse response = null;
        try{
            //execute post request
            response = httpClient.execute(httpPost);
        }catch (ClientProtocolException cpException) {
            cpException.printStackTrace();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK){
            System.out.println("Error Authentication to Salesforce dul! --> "+statusCode);
            return;
        }

        String getResult = null;
        try{
            getResult = EntityUtils.toString(response.getEntity());
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;
        try{
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        }catch (JSONException jsonException){
            jsonException.printStackTrace();
        }

        baseUri = loginInstanceUrl + restEndPoint;
        oauthHeader = new BasicHeader("Authorization", "Bearer " + loginAccessToken);
        System.out.println("respone getStatusLine --> "+response.getStatusLine());
        System.out.println("Succesfull Login");
        System.out.println("login instance url --> "+ loginInstanceUrl);
        System.out.println("access token / session id --> "+loginAccessToken);
        System.out.println("Uri --> "+baseUri);


        //release connection
        httpPost.releaseConnection();
        AccountQuery();
    }

    public static void AccountQuery(){
        System.out.println("Account query.");
        System.out.println("Account query..");
        System.out.println("Account query...");
        System.out.println("Account query.....");

        //try query from salesforce
        try{
            //setup Http objects to create request
            HttpClient httpClient = HttpClientBuilder.create().build();

            String uri = baseUri + "/query?q=SELECT+Id,Tenant_Id__c,Name+FROM+Account+Limit+2";
            System.out.println("Query Url --> " +uri);
            HttpGet httpGet = new HttpGet(uri);
            System.out.println("Oauth Header --> "+oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            //make request
            HttpResponse response = httpClient.execute(httpGet);

            //process result
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode == 200){
                String response_string = EntityUtils.toString(response.getEntity());
                try{
                    JSONObject json = new JSONObject(response_string);
                    System.out.println("Response Query:\n " +json.toString(1));
                    JSONArray j = json.getJSONArray("records");
                    for (int i = 0; i <j.length(); i++){
                        accountId = json.getJSONArray("records").getJSONObject(i).getString("Id");
                        accountName = json.getJSONArray("records").getJSONObject(i).getString("Name");
                        accountTenantId = json.getJSONArray("records").getJSONObject(i).optString ("Tenant_Id__c",null); //the Tenant_Id__c is sometimes return null, therefore must put default value
                        System.out.println("Account Id: "+ accountId +" Name: " + accountName +  " Tenant_Id: " + accountTenantId);
                    }

                }catch (JSONException je){
                    je.printStackTrace();
                }
            }else {
                System.out.println("Query not success. Response status is " + statusCode);
                System.out.println("AN error has occured. Http Status --> "+ response.getStatusLine().getStatusCode());
                System.out.println(getBody(response.getEntity().getContent()));
                System.exit(-1);
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }

    private static String getBody(InputStream inputStream){
        String result = "";
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = in.readLine()) !=null){
                result += inputLine;
                result += "\n";
            }
            in.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        return result;
    }


}
