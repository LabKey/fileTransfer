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

import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;

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
}