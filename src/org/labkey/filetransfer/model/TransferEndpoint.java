package org.labkey.filetransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by susanh on 5/14/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferEndpoint
{
    private String id;
    private String displayName;
    private String path;
    private String localDirectory;

    public TransferEndpoint()
    {}

    public TransferEndpoint(String id, String path)
    {
        setId(id);
        setPath(path);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDisplayName()
    {
        return displayName == null ? id : displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public void setDisplay_name(String displayName) { setDisplayName(displayName); }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getLocalDirectory()
    {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory)
    {
        this.localDirectory = localDirectory;
    }
}
