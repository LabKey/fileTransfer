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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.exp.list.ListService;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.Path;
import org.labkey.api.webdav.WebdavResolver;
import org.labkey.api.webdav.WebdavResolverImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileTransferManager
{
    private static final FileTransferManager _instance = new FileTransferManager();
    public static final String FILE_TRANSFER_CONFIG_PROPERTIES = "fileTransferConfigProperties";
    public static final String LOCAL_FILES_DIRECTORY = "localFilesDirectory";
    public static final String REFERENCE_FOLDER = "listFolder";
    public static final String REFERENCE_LIST = "listTable";
    public static final String REFERENCE_COLUMN = "fileNameColumn";
    public static final String SOURCE_ENDPOINT_DIRECTORY = "sourceEndpointDir";

    private FileTransferManager()
    {
        // prevent external construction with a private default constructor
    }

    public static FileTransferManager get()
    {
        return _instance;
    }

    public Boolean isMetadataListConfigured(Map<String, String> properties)
    {
        return properties != null && !properties.isEmpty();
    }

    @Nullable
    public ListDefinition getMetadataList(Map<String, String> map)
    {
        if (map != null && !map.isEmpty())
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

    public String getEndpointPath(Container container)
    {
        PropertyManager.PropertyMap map = PropertyManager.getWritableProperties(container, FILE_TRANSFER_CONFIG_PROPERTIES, true);
        return map.get(LOCAL_FILES_DIRECTORY);
    }

    public String getSourceEndpointDir(Container container)
    {
        PropertyManager.PropertyMap map = PropertyManager.getWritableProperties(container, FILE_TRANSFER_CONFIG_PROPERTIES, true);
        return map.get(SOURCE_ENDPOINT_DIRECTORY);
    }

    public WebdavResolver.LookupResult getDavResource(Container container)
    {
        Path path =  getDavPath(container);
        return WebdavResolverImpl.get().lookupEx(path);
    }

    public Path getDavPath(Container container)
    {
        return new Path("_webdav").append(container.getParsedPath()).append(FileTransferWebdavProvider.FILE_LINK);
    }

    public List<String> getActiveFiles(Container container)
    {
        List<String> activeFiles = new ArrayList<>();
        WebdavResolver.LookupResult lookupResult = FileTransferManager.get().getDavResource(container);
        if (lookupResult != null && lookupResult.resource != null && lookupResult.resource instanceof FileTransferWebdavProvider.FileTransferFolderResource)
        {
            FileTransferWebdavProvider.FileTransferFolderResource fileResources = (FileTransferWebdavProvider.FileTransferFolderResource) lookupResult.resource;
            File fileDirectory = fileResources.getFile();
            if (fileDirectory != null)
            {
                File[] files = fileDirectory.listFiles();
                if (files != null)
                {
                    for (File file : files)
                    {
                        if (file.isFile())
                            activeFiles.add(file.getName());
                    }
                }
            }
        }
        return activeFiles;
    }

    public String getServiceBaseUrl(Container container)
    {
        Module module = ModuleLoader.getInstance().getModule(FileTransferModule.NAME);
        return module.getModuleProperties().get(FileTransferModule.FILE_TRANSFER_SERVICE_BASE_URL).getEffectiveValue(container);
    }

    public String getSourceEndpointId(Container container)
    {
        Module module = ModuleLoader.getInstance().getModule(FileTransferModule.NAME);
        return module.getModuleProperties().get(FileTransferModule.FILE_TRANSFER_SOURCE_ENDPOINT_ID).getEffectiveValue(container);
    }

    public String getClientId(Container container)
    {
        Module module = ModuleLoader.getInstance().getModule(FileTransferModule.NAME);
        return module.getModuleProperties().get(FileTransferModule.FILE_TRANSFER_CLIENT_ID).getEffectiveValue(container);
    }

    public String getClientSecret(Container container)
    {
        Module module = ModuleLoader.getInstance().getModule(FileTransferModule.NAME);
        return module.getModuleProperties().get(FileTransferModule.FILE_TRANSFER_CLIENT_SECRET).getEffectiveValue(container);
    }

    public boolean isTransferConfigured(Container container)
    {
        String clientId = getClientId(container);
        String clientSecret = getClientSecret(container);
        String baseUrl = getServiceBaseUrl(container);
        return clientId != null && clientSecret != null && baseUrl != null;
    }

    public String getGlobusTransferUiUrl(Container container)
    {
        String baseUrl = getServiceBaseUrl(container);
        String endpointId = getSourceEndpointId(container);
        if (StringUtils.isNotBlank(baseUrl) && StringUtils.isNotBlank(endpointId))
        {
            // ex: https://www.globus.org/app/transfer?origin_id=<ENDPOINT_ID>&origin_path=<ENDPOINT_DIR>
            String transferUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("?") ? "?" : "")
                    + "origin_id=" + PageFlowUtil.encode(endpointId.trim());

            String endpointDir = getSourceEndpointDir(container);
            if (StringUtils.isNotBlank(endpointDir))
                transferUrl += "&origin_path=" + PageFlowUtil.encode(endpointDir.trim());

            return transferUrl;
        }

        return null;
    }
}