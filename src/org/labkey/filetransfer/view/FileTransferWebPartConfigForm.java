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
 * Created by xingyang on 2/8/17.
 */
public class FileTransferWebPartConfigForm extends ReturnUrlForm
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
