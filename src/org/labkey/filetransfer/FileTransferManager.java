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
import org.labkey.api.data.DataRegionSelection;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.exp.list.ListService;
import org.labkey.api.query.FieldKey;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.config.FileTransferSettings;
import org.labkey.filetransfer.globus.GlobusFileTransferProvider;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.provider.Registry;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.labkey.api.data.DataRegionSelection.DATA_REGION_SELECTION_KEY;

public class FileTransferManager
{
    private static final FileTransferManager _instance = new FileTransferManager();
    public static final String FILE_TRANSFER_CONFIG_PROPERTIES = "fileTransferConfigProperties";
    public static final String LOCAL_FILES_DIRECTORY = "localFilesDirectory";
    public static final String REFERENCE_FOLDER = "listFolder";
    public static final String REFERENCE_LIST = "listTable";
    public static final String REFERENCE_COLUMN = "fileNameColumn";
    public static final String SOURCE_ENDPOINT_DIRECTORY = "sourceEndpointDir";

    public static final String WEB_PART_ID_SESSION_KEY = "fileTransferWebPartId";
    public static final String RETURN_URL_SESSION_KEY = "fileTransferReturnUrl";
    public static final String FILE_TRANSFER_PROVIDER = "fileTransferProvider";
    public static final String ENDPOINT_ID_SESSION_KEY = "destinationEndpointId";
    public static final String ENDPOINT_PATH_SESSION_KEY = "destinationEndpointPath";

    public enum ErrorCode
    {
        noProvider
    }

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

    private Map<String, String> getWebPartProperties(ViewContext context)
    {
        Integer webPartId = (Integer) context.getRequest().getSession().getAttribute(WEB_PART_ID_SESSION_KEY);
        if (webPartId == null)
            return Collections.emptyMap();
        Portal.WebPart webPart =  Portal.getPart(context.getContainer(), webPartId);
        return webPart.getPropertyMap();
    }

    public FileTransferProvider getProvider(ViewContext context)
    {
        Map<String, String> properties = getWebPartProperties(context);
        return Registry.get().getProvider(context.getContainer(), context.getUser(), properties.get(FILE_TRANSFER_PROVIDER));
    }

    public TransferEndpoint getSourceEndpoint(ViewContext context)
    {
        FileTransferProvider provider = getProvider(context);
        if (provider == null)
            return null;

        FileTransferSettings settings = provider.getSettings();
        TransferEndpoint endpoint = settings.getEndpoint();
        Map<String, String> properties = getWebPartProperties(context);
        endpoint.setPath(properties.get(SOURCE_ENDPOINT_DIRECTORY));
        return endpoint;
    }

    public List<String> getFileNames(ViewContext context)
    {
        HttpSession session = context.getSession();
        // TODO do we need to get the container from the session as well?
        String key = (String) session.getAttribute(DATA_REGION_SELECTION_KEY);
        Set<Integer> selectedInts = new HashSet<>();
        for (String selection: DataRegionSelection.getSelected(context, key, true, false))
        {
            selectedInts.add(Integer.parseInt(selection));
        }
        Map<String, String> properties = getWebPartProperties(context);
        ListDefinition listDef = FileTransferManager.get().getMetadataList(properties);
        if (listDef != null)
        {
            SimpleFilter filter = new SimpleFilter(new SimpleFilter.InClause(FieldKey.fromParts(listDef.getKeyName()), selectedInts));
            Sort sort = new Sort(FieldKey.fromParts(properties.get(REFERENCE_COLUMN)));
            TableInfo tableInfo = listDef.getTable(context.getUser());
            if (tableInfo != null)
            {
                TableSelector selector = new TableSelector(tableInfo, Collections.singleton(properties.get(REFERENCE_COLUMN)), filter, sort);
                return selector.getArrayList(String.class);
            }
        }
        return Collections.emptyList();
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

    public TransferEndpoint getDestinationEndpoint(ViewContext context) throws IOException, URISyntaxException
    {
        String id = (String) context.getSession().getAttribute(ENDPOINT_ID_SESSION_KEY);
        String path = (String) context.getSession().getAttribute(ENDPOINT_PATH_SESSION_KEY);
        TransferEndpoint endpoint = null;
        if (id != null && path != null)
        {
            GlobusFileTransferProvider provider = new GlobusFileTransferProvider(context.getContainer(), context.getUser());
            endpoint = provider.getEndpoint(id);

            if (endpoint != null)
            {
                endpoint.setPath((String) context.getSession().getAttribute(ENDPOINT_PATH_SESSION_KEY));
            }

        }
        return endpoint;
    }

    public List<String> getActiveFiles(File localDir)
    {
        List<String> activeFiles = new ArrayList<>();
        if (localDir != null)
        {
            File[] directoryFiles = localDir.listFiles();
            if (directoryFiles != null)
            {
                for (File file : directoryFiles)
                {
                    activeFiles.add(file.getName());
                }
            }
        }
        return activeFiles;
    }

    public File getLocalFilesDirectory(ViewContext context)
    {
        FileTransferProvider provider = getProvider(context);
        Map<String, String> properties = getWebPartProperties(context);
        if (provider == null || properties.get(LOCAL_FILES_DIRECTORY) == null || provider.getSettings() == null || provider.getSettings().getFileTransferRoot() == null)
            return null;
        return new File(provider.getSettings().getFileTransferRoot(), properties.get(LOCAL_FILES_DIRECTORY));
    }
}