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
