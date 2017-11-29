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

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.labkey.api.action.NullSafeBindException;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.UserSchema;
import org.labkey.api.study.Dataset;
import org.labkey.api.study.StudyService;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.BaseWebPartFactory;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.WebPartConfigurationException;
import org.labkey.api.view.WebPartView;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.labkey.study.controllers.BaseStudyController;
import org.labkey.study.controllers.StudyController;
import org.labkey.study.model.DatasetDefinition;
import org.labkey.study.model.QCStateSet;
import org.labkey.study.model.StudyManager;
import org.labkey.study.query.DatasetQuerySettings;
import org.labkey.study.query.DatasetQueryView;
import org.labkey.study.query.StudyQuerySchema;
import org.springframework.validation.BindException;

import java.util.Map;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferMetadataWebPartFactory extends BaseWebPartFactory
{
    private static final String NAME = "File Transfer";
    private static final Logger LOG = Logger.getLogger(FileTransferMetadataWebPartFactory.class);


    public FileTransferMetadataWebPartFactory()
    {
        super(NAME, true, true);
    }

    @Override
    public WebPartView getWebPartView(@NotNull ViewContext context, @NotNull Portal.WebPart webPart) throws WebPartConfigurationException
    {
        Map<String, String> propertyMap = webPart.getPropertyMap();
        WebPartView view = new JspView("/org/labkey/filetransfer/view/fileList.jsp");
        view.setTitle(propertyMap.getOrDefault("webpart.title", NAME));
        FileTransferManager manager = FileTransferManager.get();
        if (manager.isMetadataListConfigured(propertyMap) && manager.isValidTransferDirectory(propertyMap))
        {

            DatasetDefinition listDef = manager.getMetadataList(propertyMap);
            if (listDef != null)
            {

                UserSchema schema = QueryService.get().getUserSchema(context.getUser(), context.getContainer(), StudyQuerySchema.SCHEMA_NAME);
                DatasetQuerySettings settings = (DatasetQuerySettings)schema.getSettings(context, DatasetQueryView.DATAREGION, listDef.getName());
                DatasetQuerySettings dqs = new DatasetQuerySettings(settings);
                BindException be = new NullSafeBindException(new Object(), "form");
                FileTransferMetadataQueryView listView = new FileTransferMetadataQueryView(webPart, schema, dqs, be);
                // coming back null...
                view.setView("metadataList", listView);
            }
        }
        return view;

    }

    public HttpView getEditView(Portal.WebPart webPart, ViewContext context)
    {
        return new FileTransferWebPartConfigView(webPart, context);
    }

    public static String getDataRegionName(DatasetDefinition listDef)
    {
        return "metadataList_" + listDef.getDatasetId();
    }

}
