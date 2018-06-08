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
package org.labkey.filetransfer.config;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.settings.AbstractWriteableSettingsGroup;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.view.FileTransferConfigForm;

/**
 * Created by susanh on 5/19/17.
 */
public class FileTransferSettings extends AbstractWriteableSettingsGroup
{
    public static final String GROUP_NAME = "FileTransfer";
    private static final String DELIMITER = "|";

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String AUTH_URL_PREFIX = "authUrlPrefix";
    private static final String TRANSFER_API_URL_PREFIX = "transferApiUrlPrefix";
    private static final String TRANSFER_UI_URL_PREFIX = "transferUrlPrefix";
    private static final String BROWSE_ENDPOINT_URL_PREFIX = "browseEndpointUrlPrefix";
    private static final String SOURCE_ENDPOINT_ID = "sourceEndpointId";
    private static final String SOURCE_ENDPOINT_NAME = "sourceEndpointName";
    private static final String SOURCE_ENDPOINT_LOCAL_FILE_ROOT = "sourceEndpointRootDir";

    private String _providerName;

    public FileTransferSettings(String providerName)
    {
        _providerName = providerName;
        makeWriteable(ContainerManager.getRoot());
    }

    @Override
    protected String getGroupName()
    {
        return _providerName + GROUP_NAME;
    }

    public String getProviderName()
    {
        return _providerName;
    }

    public void setProviderName(String providerName)
    {
        _providerName = providerName;
    }

    @Override
    protected String getType()
    {
        return "File transfer properties";
    }

    public String getFileTransferRoot()
    {
        return getRawPropertyValue(SOURCE_ENDPOINT_LOCAL_FILE_ROOT);
    }

    public String getClientId()
    {
        return getRawPropertyValue(CLIENT_ID);
    }

    public String getClientSecret()
    {
        return getRawPropertyValue(CLIENT_SECRET);
    }

    public String getAuthUrlPrefix()
    {
        return getRawPropertyValue(AUTH_URL_PREFIX);
    }

    public String getTransferApiUrlPrefix()
    {
        return getRawPropertyValue(TRANSFER_API_URL_PREFIX);
    }

    public String getTransferUiUrlPrefix()
    {
        return getRawPropertyValue(TRANSFER_UI_URL_PREFIX);
    }

    public String getBrowseEndpointUrlPrefix()
    {
        return getRawPropertyValue(BROWSE_ENDPOINT_URL_PREFIX);
    }

    public TransferEndpoint getEndpoint()
    {
        String endpointId = getRawPropertyValue(SOURCE_ENDPOINT_ID);
        String name = getRawPropertyValue(SOURCE_ENDPOINT_NAME);
        TransferEndpoint endpoint= new TransferEndpoint(endpointId, null);
        // We set the display name to the endpointId if a name was not provided.
        // Though it might be nice to retrieve the name of the endpoint from Globus, we don't
        // necessarily have credentials for queries at this point.
        endpoint.setDisplayName(StringUtils.isEmpty(name) ? endpointId : name);
        endpoint.setLocalDirectory(getRawPropertyValue(SOURCE_ENDPOINT_LOCAL_FILE_ROOT));
        return endpoint;
    }

    @Override
    public void storeStringValue(String name, @Nullable String value)
    {
        super.storeStringValue(getGroupName() + DELIMITER + name, value);
    }

    public String getRawPropertyValue(String propertyName)
    {
        return getProperties().get(getGroupName() + DELIMITER + propertyName);
    }

    public void saveProperties(FileTransferConfigForm form)
    {
        storeStringValue(SOURCE_ENDPOINT_LOCAL_FILE_ROOT, form.getSourceEndpointLocalDir());
        storeStringValue(CLIENT_ID, form.getClientId());
        storeStringValue(CLIENT_SECRET, form.getClientSecret());
        storeStringValue(AUTH_URL_PREFIX, form.getAuthUrlPrefix());
        storeStringValue(TRANSFER_API_URL_PREFIX, form.getTransferApiUrlPrefix());
        storeStringValue(TRANSFER_UI_URL_PREFIX, form.getTransferUiUrlPrefix());
        storeStringValue(BROWSE_ENDPOINT_URL_PREFIX, form.getBrowseEndpointUrlPrefix());
        storeStringValue(SOURCE_ENDPOINT_ID, form.getSourceEndpointId());
        storeStringValue(SOURCE_ENDPOINT_NAME, form.getSourceEndpointDisplayName());
        save();

    }
}
