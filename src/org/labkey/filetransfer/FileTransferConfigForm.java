package org.labkey.filetransfer;

import org.labkey.api.action.ReturnUrlForm;

/**
 * Created by xingyang on 2/8/17.
 */
public class FileTransferConfigForm extends ReturnUrlForm
{
    private String endpointPath;
    private String lookupContainer;
    private String queryName;
    private String columnName;
    private String sourceEndpointDir;

    public String getEndpointPath()
    {
        return endpointPath;
    }

    public void setEndpointPath(String filePath)
    {
        this.endpointPath = filePath;
    }

    public String getLookupContainer()
    {
        return lookupContainer;
    }

    public void setLookupContainer(String container)
    {
        this.lookupContainer = container;
    }

    public String getQueryName()
    {
        return queryName;
    }

    public void setQueryName(String queryName)
    {
        this.queryName = queryName;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public String getSourceEndpointDir()
    {
        return sourceEndpointDir;
    }

    public void setSourceEndpointDir(String sourceEndpointDir)
    {
        this.sourceEndpointDir = sourceEndpointDir;
    }
}
