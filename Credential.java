package sfdc_rest;

public class Credential {
    private final String userName = "";
    private final String password = "";
    private final String clientSecret = "";
    private final String clientKey  = "";
    private final String urlLogin = ";

    public String getUrlLogin(){
        return urlLogin;
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public String getClientSecret(){
        return clientSecret;
    }

    public String getClientKey(){
        return clientKey;
    }

}
