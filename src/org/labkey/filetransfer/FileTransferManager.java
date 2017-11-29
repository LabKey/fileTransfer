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

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.collections.NamedObject;
import org.labkey.api.collections.NamedObjectList;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DataRegionSelection;
import org.labkey.api.data.Filter;
import org.labkey.api.data.Results;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.data.TableViewForm;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryException;
import org.labkey.api.study.Dataset;
import org.labkey.api.study.Study;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.config.FileTransferSettings;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.provider.Registry;
import org.labkey.study.model.DatasetDefinition;
import org.labkey.study.model.StudyManager;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.labkey.api.data.DataRegionSelection.DATA_REGION_SELECTION_KEY;

public class FileTransferManager
{
    private static final FileTransferManager _instance = new FileTransferManager();
    public static final String LOCAL_FILES_DIRECTORY = "localFilesDirectory";
    public static final String REFERENCE_FOLDER = "listFolder";
    public static final String REFERENCE_DATASET = "listTable";
    public static final String REFERENCE_COLUMN = "fileNameColumn";
    public static final String SOURCE_ENDPOINT_DIRECTORY = "sourceEndpointDir";

    public static final String WEB_PART_ID_SESSION_KEY = "fileTransferWebPartId";
    public static final String FILE_TRANSFER_CONTAINER = "fileTransferContainer";
    public static final String RETURN_URL_SESSION_KEY = "fileTransferReturnUrl";
    public static final String FILE_TRANSFER_PROVIDER = "fileTransferProvider";
    public static final String ENDPOINT_ID_SESSION_KEY = "destinationEndpointId";
    public static final String ENDPOINT_PATH_SESSION_KEY = "destinationEndpointPath";

    //FTM streamline: keep a "default" destination as a convenience for user who haven't selected one manually
    private static TransferEndpoint defaultDestinationEndpoint = null;

    private static final Logger LOG = Logger.getLogger(FileTransferManager.class);


    public enum ErrorCode
    {
        noProvider,
        noTokens
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

    public Container getContainer(ViewContext context)
    {

        String containerId = (String) context.getRequest().getSession().getAttribute(FILE_TRANSFER_CONTAINER);
        if (containerId == null)
            return null;
        return ContainerManager.getForId(containerId);


    }

    private Map<String, String> getWebPartProperties(ViewContext context)
    {
        Integer webPartId = (Integer) context.getRequest().getSession().getAttribute(WEB_PART_ID_SESSION_KEY);
        if (webPartId == null)
            return Collections.emptyMap();
        Container container = getContainer(context);
        if (container == null)
            return Collections.emptyMap();
        Portal.WebPart webPart =  Portal.getPart(container, webPartId);
        return webPart.getPropertyMap();
    }

    public FileTransferProvider getProvider(ViewContext context)
    {
        return getProvider(getWebPartProperties(context), context);
    }

    public FileTransferProvider getProvider(Map<String, String> properties)
    {
        return Registry.get().getProvider(properties.get(FILE_TRANSFER_PROVIDER));
    }

    public FileTransferProvider getProvider(Map<String, String> properties, ViewContext context)
    {
        return Registry.get().getProvider(context.getContainer(), context.getUser(), properties.get(FILE_TRANSFER_PROVIDER));
    }

    public TransferEndpoint getSourceEndpoint(Map<String, String> properties)
    {
        FileTransferProvider provider = getProvider(properties);
        if (provider == null)
            return null;

        FileTransferSettings settings = provider.getSettings();
        TransferEndpoint endpoint = settings.getEndpoint();
        endpoint.setPath(properties.get(SOURCE_ENDPOINT_DIRECTORY));
        return endpoint;
    }

    public TransferEndpoint getSourceEndpoint(ViewContext context)
    {
        return getSourceEndpoint(getWebPartProperties(context));
    }

    public List<String> getFileNames(ViewContext context)
    {
        if(context != null)
        {
            HttpSession session = context.getSession();
            Container sessionContainer = getContainer(context);
            if (sessionContainer != null)
            {
                context.setContainer(getContainer(context));
            }
            String key = (String) session.getAttribute(DATA_REGION_SELECTION_KEY);
            Map<String, String> properties = getWebPartProperties(context);
            DatasetDefinition ddef = getMetadataList(properties);
            Dataset dataset = getReferenceDataset(properties);

            if (ddef != null && dataset != null)
            {
                Set<String> selectedVals = DataRegionSelection.getSelected(context, key, true, false);
                SimpleFilter filter = new SimpleFilter(new SimpleFilter.InClause(FieldKey.fromParts("lsid"), selectedVals));
                Sort sort = new Sort(FieldKey.fromParts(properties.get(REFERENCE_COLUMN)));
                TableInfo tableInfo = dataset.getTableInfo(context.getUser());
                if (tableInfo != null)
                {
                    TableSelector selector = new TableSelector(tableInfo, Collections.singleton(properties.get(REFERENCE_COLUMN)), filter, sort);
                    //we may end up with duplicates, even though we are using a Set of selectedVals
                    ArrayList<String> selected = selector.getArrayList(String.class);
                    //uniquify the return list
                    Set<String> unduped = new HashSet<>();
                    unduped.addAll(selected);
                    selected.clear();
                    selected.addAll(unduped);

                    return selected;
                }
            }
            return Collections.emptyList();
        }


        LOG.error("getFiles is returning nothing.  Either the user selected nothing and hit 'transfer', or something has gone terribly wrong.");
        return Collections.emptyList();
    }

    //here is where we probably need to return a dataset or query definition instead of a list
    @Nullable
    public DatasetDefinition getMetadataList(Map<String, String> map)
    {
        if (map != null && !map.isEmpty())
        {
            String listContainerId = map.get(REFERENCE_FOLDER);
            Container listContainer = ContainerManager.getForId(listContainerId);

            if (listContainer != null)
            {
                String listName = map.get(REFERENCE_DATASET);
                Study study = StudyManager.getInstance().getStudy(listContainer);
                if (study != null)
                {
                    DatasetDefinition dd = StudyManager.getInstance().getDatasetDefinitionByLabel(study, listName);
                    return dd;
                }
            }
        }
        LOG.error("getMetadataList is returning null ");
        return null;
    }

    @Nullable
    public Dataset getReferenceDataset(Map<String, String> map)
    {
        if (map != null && !map.isEmpty())
        {
            String listContainerId = map.get(REFERENCE_FOLDER);
            Container listContainer = ContainerManager.getForId(listContainerId);

            if (listContainer != null)
            {
                String datasetName = map.get(REFERENCE_DATASET);
                Study study = StudyManager.getInstance().getStudy(listContainer);
                if (study != null)
                {
                    Dataset refDataset = study.getDatasetByName(datasetName);

                    if(refDataset != null)
                    {
                        return refDataset;
                    }
                    else
                    {
                        LOG.error("getReferenceDataset dataset was null (1)");
                        return null;
                    }
                }
            }
        }
        LOG.error("getFileDataset returning null(2) ");
        return null;
    }

    public TransferEndpoint getDestinationEndpoint(ViewContext context) throws IOException, URISyntaxException
    {
        String id = (String) context.getSession().getAttribute(ENDPOINT_ID_SESSION_KEY);
        String path = (String) context.getSession().getAttribute(ENDPOINT_PATH_SESSION_KEY);
        TransferEndpoint endpoint = null;
        if (id != null && path != null)
        {
            FileTransferProvider provider = getProvider(context);
            endpoint = provider.getEndpoint(id);

            if (endpoint != null)
            {
                endpoint.setPath((String) context.getSession().getAttribute(ENDPOINT_PATH_SESSION_KEY));
            }
            //FTM streamline: instead of returning null, return the default (which can still be null)
            else
            {
                return getDefaultDestinationEndpoint();
            }

        }
        return endpoint;
    }

    /**
    * FTM streamline: getter and setter for default destination.
    * Set after authentication, using globus search API call.
    * Get in fileTransfer view for users that haven't selected a destination manually.
    * */
    public TransferEndpoint getDefaultDestinationEndpoint()
    {
        return defaultDestinationEndpoint;
    }
    public void setDefaultDestinationEndpoint(TransferEndpoint destination)
    {
        defaultDestinationEndpoint = destination;
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

    public boolean isValidTransferDirectory(Map<String, String> properties)
    {
        File webPartFileDirectory = getLocalFilesDirectory(properties);
        if (webPartFileDirectory == null)
            return false;
        FileTransferProvider provider = getProvider(properties);
        if (provider == null || provider.getSettings() == null)
            return false;
        TransferEndpoint endpoint = provider.getSettings().getEndpoint();
        if (endpoint == null)
            return false;
        if (endpoint.getLocalDirectory() == null)
            return false;
        File rootDir = new File(endpoint.getLocalDirectory());

        if (!webPartFileDirectory.toPath().normalize().startsWith(rootDir.toPath().normalize()))
            return false;
        return webPartFileDirectory.exists() && webPartFileDirectory.canRead();
    }

    public File getLocalFilesDirectory(Map<String, String> properties)
    {
        FileTransferProvider provider = getProvider(properties);
        if (provider == null || properties.get(LOCAL_FILES_DIRECTORY) == null || provider.getSettings() == null)
            return null;
        TransferEndpoint endpoint = provider.getSettings().getEndpoint();
        if (endpoint == null)
            return null;
        return new File(endpoint.getLocalDirectory(), properties.get(LOCAL_FILES_DIRECTORY));
    }
}