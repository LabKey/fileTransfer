package org.labkey.filetransfer.model;

import java.util.List;

/**
 * Created by susanh on 5/14/17.
 */
public class TransferBean
{
    private Boolean authorized;
    private List<String> fileNames;
    private String returnUrl;
    private TransferEndpoint source;
    private TransferEndpoint destination;
    private String providerName;
    private String transferResultMsg;
    private String browseEndpointsUrl;
    private String label;

    public Boolean getAuthorized()
    {
        return authorized;
    }

    public void setAuthorized(Boolean authorized)
    {
        this.authorized = authorized;
    }

    public List<String> getFileNames()
    {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames)
    {
        this.fileNames = fileNames;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }

    public TransferEndpoint getSource()
    {
        return source;
    }

    public void setSource(TransferEndpoint source)
    {
        this.source = source;
    }

    public TransferEndpoint getDestination()
    {
        return destination;
    }

    public void setDestination(TransferEndpoint destination)
    {
        this.destination = destination;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    public String getTransferResultMsg()
    {
        return transferResultMsg;
    }

    public void setTransferResultMsg(String transferResultMsg)
    {
        this.transferResultMsg = transferResultMsg;
    }

    public String getBrowseEndpointsUrl()
    {
        return browseEndpointsUrl;
    }

    public void setBrowseEndpointsUrl(String browseEndpointsUrl)
    {
        this.browseEndpointsUrl = browseEndpointsUrl;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

}
