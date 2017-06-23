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
package org.labkey.filetransfer.view;

import org.labkey.api.action.ReturnUrlForm;

/**
 * Created by susanh on 5/22/17.
 */
public class FileTransferConfigForm extends ReturnUrlForm
{
    private String sourceEndpointLocalDir;
    private String name;
    private String clientId;
    private String clientSecret;
    private String authUrlPrefix;
    private String transferApiUrlPrefix;
    private String transferUiUrlPrefix;
    private String browseEndpointUrlPrefix;
    private String sourceEndpointId;
    private String sourceEndpointDisplayName;

    public String getSourceEndpointLocalDir()
    {
        return sourceEndpointLocalDir;
    }

    public void setSourceEndpointLocalDir(String sourceEndpointLocalDir)
    {
        this.sourceEndpointLocalDir = sourceEndpointLocalDir;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getClientSecret()
    {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    public String getSourceEndpointId()
    {
        return sourceEndpointId;
    }

    public void setSourceEndpointId(String sourceEndpointId)
    {
        this.sourceEndpointId = sourceEndpointId;
    }

    public String getSourceEndpointDisplayName()
    {
        return sourceEndpointDisplayName;
    }

    public void setSourceEndpointDisplayName(String sourceEndpointDisplayName)
    {
        this.sourceEndpointDisplayName = sourceEndpointDisplayName;
    }

    public String getAuthUrlPrefix()
    {
        return authUrlPrefix;
    }

    public void setAuthUrlPrefix(String authUrlPrefix)
    {
        this.authUrlPrefix = authUrlPrefix;
    }

    public String getTransferApiUrlPrefix()
    {
        return transferApiUrlPrefix;
    }

    public void setTransferApiUrlPrefix(String transferApiUrlPrefix)
    {
        this.transferApiUrlPrefix = transferApiUrlPrefix;
    }

    public String getTransferUiUrlPrefix()
    {
        return transferUiUrlPrefix;
    }

    public void setTransferUiUrlPrefix(String transferUiUrlPrefix)
    {
        this.transferUiUrlPrefix = transferUiUrlPrefix;
    }

    public String getBrowseEndpointUrlPrefix()
    {
        return browseEndpointUrlPrefix;
    }

    public void setBrowseEndpointUrlPrefix(String browseEndpointUrlPrefix)
    {
        this.browseEndpointUrlPrefix = browseEndpointUrlPrefix;
    }
}
