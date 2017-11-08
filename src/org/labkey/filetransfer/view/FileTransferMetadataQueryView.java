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

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.Container;
import org.labkey.api.data.DataRegionSelection;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.UserSchema;
import org.labkey.api.study.Study;
import org.labkey.api.study.StudyService;
import org.labkey.api.util.Button;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.StringExpression;
import org.labkey.api.util.StringExpressionFactory;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.DataView;
import org.labkey.api.view.Portal;
import org.labkey.api.view.SimpleTextDisplayElement;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.provider.Registry;
import org.labkey.filetransfer.query.FileTransferMetadataTable;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.labkey.study.model.DatasetDefinition;
import org.labkey.study.query.DatasetQuerySettings;
import org.labkey.study.query.DatasetQueryView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.log4j.Logger;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.labkey.filetransfer.FileTransferManager.FILE_TRANSFER_PROVIDER;

/**
 * Created by susanh on 5/9/17.
 */
// public class FileTransferMetadataQueryView extends QueryView
public class FileTransferMetadataQueryView extends DatasetQueryView
{
    Portal.WebPart webPart;
    Map<String, String> properties;
    DatasetDefinition listDef;
    FileTransferProvider provider;
    private static final Logger LOG = Logger.getLogger(FileTransferMetadataQueryView.class);

    public FileTransferMetadataQueryView(Portal.WebPart webPart, UserSchema schema, DatasetQuerySettings settings, BindException errors)
    {
        super(schema, settings, errors);
        this.webPart = webPart;
        this.properties = webPart.getPropertyMap();
        if (this.properties != null)
        {
            listDef = FileTransferManager.get().getMetadataList(this.properties);
            provider = Registry.get().getProvider(getContainer(), getUser(), this.properties.get(FILE_TRANSFER_PROVIDER));
        }
        if (!FileTransferManager.get().isValidTransferDirectory(this.properties))
        {
            listDef = null;
        }

        for(ObjectError e : errors.getAllErrors())
        {
            LOG.error("FileTransferMetadataQueryView ctor errors:");
            LOG.error(e.getCode());
            LOG.error(e.getObjectName());
            LOG.error(e);
        }



    }


    @Override
    protected void populateButtonBar(DataView view, ButtonBar bar)
    {
        super.populateButtonBar(view, bar);
        if (provider == null)
            return;

        if (provider.isTransferApiConfigured())
        {
            String transferUrl = new ActionURL(FileTransferController.AuthAction.class, view.getViewContext().getContainer())
                    .addParameter(DataRegionSelection.DATA_REGION_SELECTION_KEY, view.getDataRegion().getSelectionKey())
                    .addParameter("webPartId", webPart.getRowId())
                    .addReturnURL(view.getViewContext().getActionURL())
                    .toString();
            if (transferUrl != null)
            {
                StringExpression url = StringExpressionFactory.createURL(transferUrl);
                StyleableActionButton transferBtn = new StyleableActionButton("Transfer", url);
                transferBtn.addClass("ftm-gridview-transfer-button");
                transferBtn.setId("ftmGridviewTransferButton");

                transferBtn.setRequiresSelection(true);
                bar.add(transferBtn);
            }
        }
        String transferLinkUrl = provider.getTransferUiUrl(properties, getViewContext());
        if (transferLinkUrl != null)
        {
            StringExpression url = StringExpressionFactory.createURL(transferLinkUrl);
            if (url != null)
            {
                SimpleTextDisplayElement jsVarView = new SimpleTextDisplayElement("<script type='text/javascript'>ftmTransferLinkUrl='"+url.toString()+"';</script>", true);
                bar.add(jsVarView);
            }
        }
    }
}
