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
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.files.FileContentService;
import org.labkey.api.portal.ProjectUrls;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.RequiresSiteAdmin;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.services.ServiceRegistry;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.filetransfer.view.FileTransferMetadataView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class FileTransferController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(FileTransferController.class);
    public static final String NAME = "filetransfer";

    public FileTransferController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return new FileTransferMetadataView(getViewContext());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    @RequiresSiteAdmin
    public class ConfigurationAction extends FormViewAction<FileTransferConfigForm>
    {
        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("File Transfer Set-Up");
            return root;
        }

        @Override
        public void validateCommand(FileTransferConfigForm form, Errors errors)
        {
            String endpointPath = form.getEndpointPath();
            FileContentService service = ServiceRegistry.get().getService(FileContentService.class);
            if (service != null && !service.isValidProjectRoot(endpointPath))
            {
                errors.reject(ERROR_MSG, "File root '" + endpointPath + "' does not appear to be a valid directory accessible to the server at " + getViewContext().getRequest().getServerName() + ".");
            }
        }

        @Override
        public ModelAndView getView(FileTransferConfigForm form, boolean reshow, BindException errors) throws Exception
        {
            Map<String, String> map = PropertyManager.getProperties(getContainer(), FileTransferManager.FILE_TRANSFER_CONFIG_PROPERTIES);
            form.setEndpointPath(map.get(FileTransferManager.ENDPOINT_DIRECTORY));
            form.setLookupContainer(map.get(FileTransferManager.REFERENCE_FOLDER));
            form.setQueryName(map.get(FileTransferManager.REFERENCE_LIST));
            form.setColumnName(map.get(FileTransferManager.REFERENCE_COLUMN));
            form.setSourceEndpointDir(map.get(FileTransferManager.SOURCE_ENDPOINT_DIRECTORY));
            return new JspView<>("/org/labkey/filetransfer/view/fileTransferConfig.jsp", form, errors);
        }

        @Override
        public boolean handlePost(FileTransferConfigForm form, BindException errors) throws Exception
        {
            if (StringUtils.isEmpty(form.getEndpointPath()))
                return false;

            if (errors.hasErrors())
                return false;

            FileTransferManager.get().saveFileTransferConfig(form, getContainer());
            return true;
        }

        @Override
        public URLHelper getSuccessURL(FileTransferConfigForm form)
        {
            if (form.getReturnUrl() != null)
                return new ActionURL(form.getReturnUrl());
            else
                return PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(getContainer());
        }
    }

}