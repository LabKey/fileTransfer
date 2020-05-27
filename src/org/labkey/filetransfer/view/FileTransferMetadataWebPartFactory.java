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

import org.jetbrains.annotations.NotNull;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.UserSchema;
import org.labkey.api.view.BaseWebPartFactory;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.WebPartConfigurationException;
import org.labkey.api.view.WebPartView;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.query.FileTransferQuerySchema;

import java.util.Map;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferMetadataWebPartFactory extends BaseWebPartFactory
{
    private static final String NAME = "File Transfer";

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
            UserSchema schema = QueryService.get().getUserSchema(context.getUser(), context.getContainer(), FileTransferQuerySchema.NAME);

            ListDefinition listDef = manager.getMetadataList(propertyMap);
            if (listDef != null)
            {
                QuerySettings settings = schema.getSettings(context, getDataRegionName(listDef), FileTransferQuerySchema.FILE_METADATA_TABLE_NAME + "_" + webPart.getRowId());
                FileTransferMetadataQueryView listView = new FileTransferMetadataQueryView(webPart, schema, settings, null);
                view.setView("metadataList", listView);
            }
        }
        return view;
    }

    @Override
    public HttpView getEditView(Portal.WebPart webPart, ViewContext context)
    {
        return new FileTransferWebPartConfigView(webPart, context);
    }

    public static String getDataRegionName(ListDefinition listDef)
    {
        return "metadataList_" + listDef.getListId();
    }
}
