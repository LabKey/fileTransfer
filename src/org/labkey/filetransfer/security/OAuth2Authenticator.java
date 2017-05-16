package org.labkey.filetransfer.security;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.AuthorizationRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.ActionURL;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/2/17.
 */
public abstract class OAuth2Authenticator
{
    private static final Logger logger = Logger.getLogger(OAuth2Authenticator.class);
    public static final String ACCESS_TOKEN = "transferToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String EXPIRE_TIME_MILLIS = "expireTimeMillis";
    public static final String FILE_TRANSFER_AUTH_CATEGORY = "fileTransferAuth";

    private Credential credential;
    private User user;
    private Container container;

    public OAuth2Authenticator(User user, Container container)
    {
        this.user = user;
        this.container = container;
    }

    protected abstract String getAuthorizationUrlPrefix();

    protected abstract String getTokensUrlPrefix();

    protected abstract List<String> getScopes();

    protected abstract Map<String, String> getAdditionalAuthParams();

    public String getAuthorizationUrl()
    {
        // https://auth.globus.org/v2/oauth2/authorize&client_id=<client_id>&redirect_uri=http://<hostname>/<project>/fileTransfer-auth.view?&response_type=code&scope=urn:globus:auth:scope:transfer.api.globus.org:all
        AuthorizationRequestUrl url = new AuthorizationRequestUrl(getAuthorizationUrlPrefix(), FileTransferManager.get().getClientId(container), Arrays.asList("code"));
        url.setScopes(getScopes());
        url.setRedirectUri(getRedirectUri());
        url.setState("XYZ"); // TODO
        for (Map.Entry<String, String> entry : getAdditionalAuthParams().entrySet())
        {
            url.set(entry.getKey(), entry.getValue());
        }
        return url.build();

    }

    public Boolean doAuthorization() throws IOException, URISyntaxException
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            URI uri = new URI(getAuthorizationUrl());
            HttpGet httpGet = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(httpGet))
            {
//                ResponseHandler<String> handler = new BasicResponseHandler();
                StatusLine status = response.getStatusLine();

                if (status.getStatusCode() != HttpStatus.SC_OK)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public String getRedirectUri()
    {
        return new ActionURL(FileTransferController.TokensAction.class, container).getURIString();
    }

    public String getClientAuthHeader() throws UnsupportedEncodingException
    {
        String clientData = String.format("%s:%s", FileTransferManager.get().getClientId(container), FileTransferManager.get().getClientSecret(container));

        return new String(Base64.encodeBase64(clientData.getBytes("UTF-8")), "UTF-8");
    }

    public BasicAuthentication getClientAuthentication()
    {
        return new BasicAuthentication(FileTransferManager.get().getClientId(container), FileTransferManager.get().getClientSecret(container));
    }

    public Credential getTokens(String authCode) throws Exception
    {
        NetHttpTransport transport = new NetHttpTransport();

        AuthorizationCodeTokenRequest tokenRequest = new AuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(), new GenericUrl(getTokensUrlPrefix()), authCode);
        tokenRequest.setRedirectUri(getRedirectUri());
        tokenRequest.setClientAuthentication(getClientAuthentication());
        TokenResponse response = tokenRequest.execute();
        if (response.getAccessToken() != null)
            return createCredentialWithRefreshToken(user, container, tokenRequest.getTransport(), tokenRequest.getJsonFactory(), response);
        else
            return null;
    }

    public Credential createCredentialWithRefreshToken(User user, Container container, HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) throws IOException
    {
        DataStore<StoredCredential> store = new SecurePropertiesDataStore(user, container);
        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                transport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(
                        new GenericUrl(getTokensUrlPrefix()))
                .setClientAuthentication(getClientAuthentication())
                .addRefreshListener(new DataStoreCredentialRefreshListener(container.getId()+":"+user.getUserId(),
                        store))
                .build()
                .setFromTokenResponse(tokenResponse);
    }


//    @Nullable
//    public TokenResponse readTokens() throws IOException
//    {
//        DataStore dataStore = SecurePropertiesDataStoreFactory.get().getDataStore(user, container);
//        PropertyManager.PropertyMap properties = PropertyManager.getEncryptedStore().getProperties(user, container, FILE_TRANSFER_AUTH_CATEGORY);
//        if (properties != null)
//        {
//            tokens  = new TokenResponse();
//            if (properties.get(ACCESS_TOKEN) != null)
//                tokens.setAccessToken(properties.get(ACCESS_TOKEN));
//            if (properties.get(REFRESH_TOKEN) != null)
//                tokens.setRefreshToken(properties.get(REFRESH_TOKEN));
//        }
//        return tokens;
//    }

    public boolean isAuthorized()
    {
        try
        {
            SecurePropertiesDataStore store = new SecurePropertiesDataStore(user, container);
            StoredCredential credential = store.get(null);
            return credential.getAccessToken() != null && credential.getExpirationTimeMilliseconds() != null && credential.getExpirationTimeMilliseconds() > 0;
        }
        catch (IOException e)
        {
            logger.error("Problem reading from credentials data store for user " + user.getUserId() + " and contianer " + container);
            return false;
        }
    }

//    public void saveTokens()
//    {
//        PropertyManager.PropertyMap properties = PropertyManager.getEncryptedStore().getWritableProperties(user, container, FILE_TRANSFER_AUTH_CATEGORY, true);
//        if (tokens.getAccessToken() != null)
//            properties.put(ACCESS_TOKEN, tokens.getAccessToken());
//        if (tokens.getRefreshToken() != null)
//            properties.put(REFRESH_TOKEN, tokens.getRefreshToken());
//        properties.save();
//    }
}
