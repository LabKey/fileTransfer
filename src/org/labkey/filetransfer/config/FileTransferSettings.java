package org.labkey.filetransfer.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.settings.AbstractWriteableSettingsGroup;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.view.FileTransferConfigForm;

import java.util.Collections;
import java.util.Map;

/**
 * Created by susanh on 5/19/17.
 */
public class FileTransferSettings extends AbstractWriteableSettingsGroup
{
    public static final String GROUP_NAME = "FileTransfer";
    private static final String DELIMITER = "|";
    private static final String FILE_TRANSFER_ROOT = "rootDirectory";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String AUTH_URL_PREFIX = "authUrlPrefix";
    private static final String TRANSFER_API_URL_PREFIX = "transferApiUrlPrefix";
    private static final String TRANSFER_UI_URL_PREFIX = "transferUrlPrefix";
    private static final String BROWSE_ENDPOINT_URL_PREFIX = "browseEndpointUrlPrefix";

    private String _providerName;

    public FileTransferSettings(String providerName)
    {
        this._providerName = providerName;
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
        this._providerName = providerName;
    }

    @Override
    protected String getType()
    {
        return "File transfer properties";
    }

    public String getFileTransferRoot()
    {
        return getRawPropertyValue(FILE_TRANSFER_ROOT);
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

    @NotNull
    public Map<String, TransferEndpoint> getEndpoints()
    {
        return Collections.emptyMap();
    }

    public TransferEndpoint getEndpoint(String propertyName)
    {
        return getEndpoints().get(propertyName);
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
        storeStringValue(FILE_TRANSFER_ROOT, form.getRootDir());
        storeStringValue(CLIENT_ID, form.getClientId());
        storeStringValue(CLIENT_SECRET, form.getClientSecret());
        storeStringValue(AUTH_URL_PREFIX, form.getAuthUrlPrefix());
        storeStringValue(TRANSFER_API_URL_PREFIX, form.getTransferApiUrlPrefix());
        storeStringValue(TRANSFER_UI_URL_PREFIX, form.getTransferUiUrlPrefix());
        storeStringValue(BROWSE_ENDPOINT_URL_PREFIX, form.getBrowseEndpointUrlPrefix());
        save();

    }
}
