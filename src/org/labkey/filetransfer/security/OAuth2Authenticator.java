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
import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.User;
import org.labkey.api.view.ActionURL;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.config.FileTransferSettings;

import java.io.IOException;
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

    private User user;
    private Container container;
    protected FileTransferSettings settings;

    public OAuth2Authenticator(User user, Container container, String name)
    {
        this.user = user;
        this.container = container;
        settings = new FileTransferSettings(name);
    }

    protected abstract String getAuthorizationUrlPrefix();

    protected abstract String getTokensUrlPrefix();

    protected abstract List<String> getScopes();

    protected abstract Map<String, String> getAdditionalAuthParams();

    public String getAuthorizationUrl()
    {
        AuthorizationRequestUrl url = new AuthorizationRequestUrl(getAuthorizationUrlPrefix(), settings.getClientId(), Arrays.asList("code"));
        url.setScopes(getScopes());
        url.setRedirectUri(getRedirectUri());
        for (Map.Entry<String, String> entry : getAdditionalAuthParams().entrySet())
        {
            url.set(entry.getKey(), entry.getValue());
        }
        return url.build();

    }

    public String getRedirectUri()
    {
        return new ActionURL(FileTransferController.TokensAction.class, ContainerManager.getRoot()).getURIString();
    }

    public BasicAuthentication getClientAuthentication()
    {
        return new BasicAuthentication(settings.getClientId(), settings.getClientSecret());
    }

    public Credential getTokens(String authCode) throws Exception
    {
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

}
