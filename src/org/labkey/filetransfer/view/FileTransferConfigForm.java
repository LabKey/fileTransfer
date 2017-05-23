package org.labkey.filetransfer.view;

import org.labkey.api.action.ReturnUrlForm;
import org.labkey.filetransfer.model.TransferEndpoint;

import java.util.Map;

/**
 * Created by susanh on 5/22/17.
 */
public class FileTransferConfigForm extends ReturnUrlForm
{
    private String rootDir;
    private String name;
    private String clientId;
    private String clientSecret;
    private String authUrlPrefix;
    private String transferApiUrlPrefix;
    private String transferUiUrlPrefix;
    private String browseEndpointUrlPrefix;
    private Map<String, TransferEndpoint> endpoints;

    public String getRootDir()
    {
        return rootDir;
    }

    public void setRootDir(String rootDir)
    {
        this.rootDir = rootDir;
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

    public Map<String, TransferEndpoint> getEndpoints()
    {
        return endpoints;
    }

    public void setEndpoints(Map<String, TransferEndpoint> endpoints)
    {
        this.endpoints = endpoints;
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
