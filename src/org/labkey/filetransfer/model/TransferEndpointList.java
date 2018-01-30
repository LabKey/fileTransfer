package org.labkey.filetransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: priggs
 * Date: 12/5/2017
 * Time: 11:47 AM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferEndpointList
{
    private TransferEndpoint[] data;
    public TransferEndpointList() {

    }
    public TransferEndpoint[] getData()
    {
        return data;
    }
    @JsonProperty("DATA")
    public void setData(TransferEndpoint[] data)
    {
        this.data = data;
    }
}
