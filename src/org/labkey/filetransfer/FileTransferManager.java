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

package org.labkey.filetransfer;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.exp.list.ListService;

public class FileTransferManager
{
    private static final FileTransferManager _instance = new FileTransferManager();
    public static final String FILE_TRANSFER_CONFIG_PROPERTIES = "fileTransferConfigProperties";
    public static final String ENDPOINT_DIRECTORY = "endpointDirectory";
    public static final String REFERENCE_FOLDER = "listFolder";
    public static final String REFERENCE_LIST = "listTable";
    public static final String REFERENCE_COLUMN = "fileColumn";

    private FileTransferManager()
    {
        // prevent external construction with a private default constructor
    }

    public static FileTransferManager get()
    {
        return _instance;
    }

    public void saveFileTransferConfig(FileTransferConfigForm form, Container container)
    {
        PropertyManager.PropertyMap map = PropertyManager.getWritableProperties(container, FILE_TRANSFER_CONFIG_PROPERTIES, true);
        map.put(ENDPOINT_DIRECTORY, String.valueOf(form.getEndpointPath()));
        map.put(REFERENCE_FOLDER, String.valueOf(form.getLookupContainer()));
        map.put(REFERENCE_LIST, String.valueOf(form.getQueryName()));
        map.put(REFERENCE_COLUMN, String.valueOf(form.getColumnName()));
        map.save();
    }

    public PropertyManager.PropertyMap getFileTransferConfig(Container container)
    {
        return PropertyManager.getProperties(container, FileTransferManager.FILE_TRANSFER_CONFIG_PROPERTIES);
    }

    public Boolean isMetadataListConfigured(Container container)
    {
        return !getFileTransferConfig(container).isEmpty();
    }

    @Nullable
    public ListDefinition getMetadataList(Container container)
    {
        PropertyManager.PropertyMap map = FileTransferManager.get().getFileTransferConfig(container);

        if (!map.isEmpty())
        {
            String listContainerId = map.get(REFERENCE_FOLDER);
            Container listContainer = ContainerManager.getForId(listContainerId);
            if (listContainer != null)
            {
                String listName = map.get(REFERENCE_LIST);
                return ListService.get().getList(listContainer, listName);
            }
        }
        return null;
    }

    public String getFileNameColumn(Container container)
    {
        PropertyManager.PropertyMap map = FileTransferManager.get().getFileTransferConfig(container);
        if (!map.isEmpty())
        {
            return map.get(REFERENCE_COLUMN);
        }
        return null;
    }

    public String getEndpointPath(Container container)
    {
        PropertyManager.PropertyMap map = PropertyManager.getWritableProperties(container, FILE_TRANSFER_CONFIG_PROPERTIES, true);
        return map.get(ENDPOINT_DIRECTORY);
    }

}