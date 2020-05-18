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

public class Main2 {

    static String grantService = "/services/oauth2/token?grant_type=password";
    private static String baseUri;
    private static Header oauthHeader;
    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint","1");
    private static String restEndPoint = "/services/data/v48.0";
    private static String opportunityAccountId;
    private static String opportunityName;
    private static String opportunityLooId;
    private static String contentDocumentId;
    private static String linkEntityId;
    private static String cdlID;

    public static void main (String [] args){

        String urlLogin = Credential.getUrlLogin();
        String usrName = Credential.getUserName();
        String pwd = Credential.getPassword();
        String clSec = Credential.getClientSecret();
        String clKey = Credential.getClientKey();

        HttpClient httpClient = HttpClientBuilder.create().build();
        String loginUrl = urlLogin + grantService + "&client_id=" + clSec + "&client_secret=" + clKey + "&username=" + usrName + "&password=" + pwd;
        System.out.println("login Url --> "+loginUrl);

        HttpPost httpPost = new HttpPost(loginUrl);
        HttpResponse response = null;
        try{
            //execute post request
            response = httpClient.execute(httpPost);
        }catch (ClientProtocolException cpException){
            cpException.printStackTrace();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

        final int statusCode= response.getStatusLine().getStatusCode();
        if(statusCode !=HttpStatus.SC_OK){
            System.out.println("Authentication to Salesforce Error Dul!!! "+statusCode);
        }

        String getResult = null;
        try{
            getResult = EntityUtils.toString(response.getEntity());
        }catch(IOException ioException){
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
        oauthHeader = new BasicHeader("Authorization", "Bearer "+loginAccessToken);
        System.out.println("respone getStatusLine --> "+response.getStatusLine());
        System.out.println("Successfully Login To Salesforce yay!");
        System.out.println("login instance url --> "+ loginInstanceUrl);
        System.out.println("access token / session id --> "+loginAccessToken);
        System.out.println("Uri --> "+baseUri);

        //release connection
        httpPost.releaseConnection();
        SalesforceDocumentQuery();
    }

    public static void SalesforceDocumentQuery() {
        System.out.println("Query Opportunity in progress......");

        //query to Opty
        try {
            //connection to sfdc
            HttpClient httpClient = HttpClientBuilder.create().build();

            String uriOpty = baseUri + "/query?q=SELECT+Name,AccountId,LOO_Id__c+FROM+Opportunity+WHERE+LOO_Id__c='LOO-KUN10-TD-2004-N-003548'";
            System.out.println("Query URL: " + uriOpty);
            HttpGet httpGet = new HttpGet(uriOpty);
            System.out.println("Oauth Header: " + oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            //execute request
            HttpResponse response = httpClient.execute(httpGet);

            //process result
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    System.out.println("Response Query:\n " + json.toString(1));
                    //JSONArray j = json.getJSONArray("records");
                    if (json.getJSONArray("records").isEmpty() == false) {
                        //for(int i = 0; i < j.length(); i++){
                        opportunityAccountId = json.getJSONArray("records").getJSONObject(0).getString("AccountId");
                        opportunityName = json.getJSONArray("records").getJSONObject(0).getString("Name");
                        opportunityLooId = json.getJSONArray("records").getJSONObject(0).getString("LOO_Id__c");
                        System.out.println("Opprtunity Account Id: " + opportunityAccountId + " Opportunity Name: " + opportunityName + " LOO ID: " + opportunityLooId);
                        //}
                    } else System.out.println("Nomor LOO gak ketemu dull, cari yang bener!!");
                } catch (JSONException jE) {
                    jE.printStackTrace();
                }
            } else {
                System.out.println("Query not success. Response status is " + statusCode);
                System.out.println("AN error has occured. Http Status --> " + response.getStatusLine().getStatusCode());
                System.out.println(getBody(response.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException iOE) {
            iOE.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        String AccountId = opportunityAccountId;
        System.out.println("This IS AccID -->  " + AccountId);


        //Query to Content Document
        System.out.println("Query ContentDocument........");
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String uriConDoc = baseUri + "/query/?q=SELECT+ContentDocumentId,ContentDocument.Title,LinkedEntityId,LinkedEntity.Name,Id+FROM+ContentDocumentLink+WHERE+LinkedEntityId=" + "'" + AccountId + "'";
            System.out.println("Query URL: " + uriConDoc);
            HttpGet httpGet = new HttpGet(uriConDoc);
            System.out.println("Oauth Header: " + oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            //execute request
            HttpResponse response1 = httpClient.execute(httpGet);

            //process result
            int statusCode1 = response1.getStatusLine().getStatusCode();
            if (statusCode1 == 200) {
                String response_string1 = EntityUtils.toString(response1.getEntity());
                try {
                    JSONObject json1 = new JSONObject(response_string1);
                    System.out.println("Response Query:\n " + json1.toString(1));
                    JSONArray j1 = json1.getJSONArray("records");
                    for (int k = 0; k < j1.length(); k++) {
                        contentDocumentId = json1.getJSONArray("records").getJSONObject(k).getString("ContentDocumentId");
                        linkEntityId = json1.getJSONArray("records").getJSONObject(k).getString("LinkedEntityId");
                        cdlID = json1.getJSONArray("records").getJSONObject(k).getString("Id");
                        System.out.println("ContentDocumentId: " + contentDocumentId + " LinkEntityId: " + linkEntityId + " ContentDocumentLinkId: " + cdlID);
                    }
                } catch (JSONException jE) {
                    jE.printStackTrace();
                }
            } else {
                System.out.println("Query not success. Response status is " + statusCode1);
                System.out.println("AN error has occured. Http Status --> " + response1.getStatusLine().getStatusCode());
                System.out.println(getBody(response1.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }



    public static String getBody (InputStream inputStream){
        String result = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
                while ((inputLine = in.readLine()) != null) {
                        result += inputLine;
                        result += "\n";
                        }
                    in.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

        return result;
    }

}