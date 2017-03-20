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
import org.labkey.api.webdav.WebdavResolver;
import org.labkey.api.webdav.WebdavResolverImpl;
import org.labkey.api.util.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTransferManager
{
    private static final FileTransferManager _instance = new FileTransferManager();
    public static final String FILE_TRANSFER_CONFIG_PROPERTIES = "fileTransferConfigProperties";
    public static final String ENDPOINT_DIRECTORY = "endpointDirectory";
    public static final String REFERENCE_FOLDER = "listFolder";
    public static final String REFERENCE_LIST = "listTable";
    public static final String REFERENCE_COLUMN = "fileColumn";
    public static final String SOURCE_ENDPOINT_DIRECTORY = "sourceEndpointDir";

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
        String oldPath = getEndpointPath(container);

        PropertyManager.PropertyMap map = PropertyManager.getWritableProperties(container, FILE_TRANSFER_CONFIG_PROPERTIES, true);
        map.put(ENDPOINT_DIRECTORY, String.valueOf(form.getEndpointPath()));
        map.put(REFERENCE_FOLDER, String.valueOf(form.getLookupContainer()));
        map.put(REFERENCE_LIST, String.valueOf(form.getQueryName()));
        map.put(REFERENCE_COLUMN, String.valueOf(form.getColumnName()));
        map.put(SOURCE_ENDPOINT_DIRECTORY, form.getSourceEndpointDir() != null ? String.valueOf(form.getSourceEndpointDir()) : null);
        map.save();

        ContainerManager.ContainerPropertyChangeEvent evt = new ContainerManager.ContainerPropertyChangeEvent(
                container, ContainerManager.Property.EndpointDirectory, oldPath, form.getEndpointPath());
        ContainerManager.firePropertyChangeEvent(evt);
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

    public String getGlobusGenomicsTransferUrl(Container container)
    {
        String baseUrl = getServiceBaseUrl(container);
        String endpointId = getSourceEndpointId(container);
        if (StringUtils.isNotBlank(baseUrl) && StringUtils.isNotBlank(endpointId))
        {
            // ex: https://www.globus.org/app/transfer?origin_id=<ENDPOINT_ID>&origin_path=<ENDPOINT_DIR>
            String transferUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("?") ? "?" : "") + "origin_id=" + endpointId.trim();

            String endpointDir = getSourceEndpointDir(container);
            if (StringUtils.isNotBlank(endpointDir))
                transferUrl += "&origin_path=" + endpointDir.trim();

            return transferUrl;
        }

        return null;
    }
}