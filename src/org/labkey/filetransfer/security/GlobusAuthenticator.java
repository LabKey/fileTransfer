package org.labkey.filetransfer.security;

import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/17.
 */
public class GlobusAuthenticator extends OAuth2Authenticator
{
    private static final String AUTH_URL_PREFIX = "https://auth.globus.org/v2/oauth2";
    private static final String TRANSFER_SCOPE = "urn:globus:auth:scope:transfer.api.globus.org:all";

    public GlobusAuthenticator(User user, Container container)
    {
        super(user, container);
    }

    protected String getAuthorizationUrlPrefix()
    {
        return AUTH_URL_PREFIX + "/authorize";
    }

    protected String getTokensUrlPrefix()
    {
        return AUTH_URL_PREFIX + "/token";
    }

    protected List<String> getScopes()
    {
        return Arrays.asList(TRANSFER_SCOPE);
    }

    protected Map<String, String> getAdditionalAuthParams()
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("access_type", "offline");
        return paramMap;
    }
}
