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
package org.labkey.filetransfer.model;

import org.labkey.filetransfer.FileTransferManager;

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
    private FileTransferManager.ErrorCode errorCode;

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

    public FileTransferManager.ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(FileTransferManager.ErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }
}
