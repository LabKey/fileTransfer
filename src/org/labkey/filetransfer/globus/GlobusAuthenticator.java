/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.filetransfer.globus;

import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.filetransfer.security.OAuth2Authenticator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/17.
 */
public class GlobusAuthenticator extends OAuth2Authenticator
{
    private static final String TRANSFER_SCOPE = "urn:globus:auth:scope:transfer.api.globus.org:all";

    public GlobusAuthenticator(User user, Container container)
    {
        super(user, container, GlobusFileTransferProvider.NAME);
    }

    @Override
    protected String getAuthorizationUrlPrefix()
    {
        return settings.getAuthUrlPrefix() + "/authorize";
    }

    @Override
    protected String getTokensUrlPrefix()
    {
        return settings.getAuthUrlPrefix() + "/token";
    }

    @Override
    protected List<String> getScopes()
    {
        return Arrays.asList(TRANSFER_SCOPE);
    }

    @Override
    protected Map<String, String> getAdditionalAuthParams()
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("access_type", "offline");
        return paramMap;
    }
}
