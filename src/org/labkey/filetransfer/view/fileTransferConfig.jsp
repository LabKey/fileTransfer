<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%
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
%>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.labkey.api.security.User" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.filetransfer.FileTransferConfigForm" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("sqv");
        dependencies.add("fileTransfer/fileTransferConfig.css");
    }
%>
<%
    Container c = getContainer();
    User user = getUser();
    JspView<FileTransferConfigForm> me = (JspView<FileTransferConfigForm>) HttpView.currentView();
    FileTransferConfigForm bean = me.getModelBean();
%>
<labkey:errors/>
<div id="SQVPicker"></div>


<script type="text/javascript">

    Ext4.onReady(function()
    {
        var getFieldHoverText = function(title, details) {
            return '<a href="#" onclick="return showHelpDiv(this, \'' + title + '\', \'' + details + '\');" '
                    + 'onmouseover="return showHelpDivDelay(this, \'' + title + '\', \'' + details + '\');" '
                    + 'onmouseout="return hideHelpDivDelay();"><span class="labkey-help-pop-up">?</span></a>';
        };

        var containingFilesHeader = Ext4.create('Ext.form.Label', {
            text: 'Files Directory Path',
            style: 'font-weight: bold;'
        });

        var endpointField = Ext4.create('Ext.form.field.Text', {
            name: "endpointPath",
            labelWidth: 150,
            width: 510,
            padding: '10px 0 25px 0',
            hidden: false,
            disabled: false,
            fieldLabel: "Local Folder Path",
            initialValue : <%=q(bean.getEndpointPath())%>,
            value: <%=q(bean.getEndpointPath())%>,
            allowBlank: false
        });

        var referenceListHeader = Ext4.create('Ext.form.Label', {
            text: 'Reference List',
            style: 'font-weight: bold;'
        });

        var sqvModel = Ext4.create('LABKEY.sqv.Model', {});

        var containerIdTextField = Ext4.create('Ext.form.field.Text', {
            name: 'containerId',
            hidden: true
        });

        var containerComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeContainerComboConfig({
            name: 'lookupContainer',
            labelWidth: 150,
            fieldLabel: 'Folder',
            editable: false,
            width: 510,
            padding: '10px 0 0 0',
            allowBlank: false,
            initialValue : <%=q(bean.getLookupContainer())%>,
            value : <%=q(bean.getLookupContainer())%>,
            listeners: {
                select: function(combo) {
                    containerIdTextField.setValue(combo.getValue());
                    sqvModel.changeQueryStore(combo.getValue(), 'lists');
                }
            }
        }));

        var schemaComboField = Ext4.create('Ext.form.field.ComboBox', {
            name: 'listSchema',
            labelWidth: 150,
            allowBlank: false,
            fieldLabel: 'Schema',
            value: 'lists',
            disabled: true,
            width: 300,
            padding: '10px 0 0 0'
        });

        var queryComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeQueryComboConfig({
            name: 'queryName',
            forceSelection: true,
            defaultSchema: 'lists',
            fieldLabel: 'List',
            labelWidth: 150,
            allowBlank: false,
            initialValue : <%=q(bean.getQueryName())%>,
            value : <%=q(bean.getQueryName())%>,
            width: 300,
            padding: '10px 0 0 0'
        }));

        var columnComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeColumnComboConfig({
            name: 'columnName',
            fieldLabel: 'File Name Field',
            forceSelection: true,
            labelWidth: 150,
            allowBlank: false,
            initialValue : <%=q(bean.getColumnName())%>,
            value : <%=q(bean.getColumnName())%>,
            margin: '0, 0, 20, 0',
            width: 300,
            padding: '10px 0 25px 0'
        }));

        var transferSourceHeader = Ext4.create('Ext.form.Label', {
            text: 'Globus Genomics File Transfer Source',
            style: 'font-weight: bold;'
        });

        var sourceEndpointDirField = Ext4.create('Ext.form.field.Text', {
            name: "sourceEndpointDir",
            labelWidth: 150,
            width: 510,
            padding: '10px 0 25px 0',
            fieldLabel: "Endpoint Directory" + getFieldHoverText('Endpoint Directory', 'Specify the directory on the '
                    + 'Globus Genomics endpoint that contains the files for this webpart.'),
            initialValue : <%=q(bean.getSourceEndpointDir())%>,
            value: <%=q(bean.getSourceEndpointDir())%>
        });

        var cancelButton = Ext4.create('Ext.button.Button', {
            text: 'Cancel',
            scope: this,
            handler: function ()
            {
                var url = LABKEY.ActionURL.getParameter("returnUrl");
                if (!url)
                    url = LABKEY.ActionURL.buildURL("project", "begin.view");

                window.location = url;
            }
        });

        var saveButton = Ext4.create('Ext.button.Button', {
            text: 'Save',
            scope: this,
            handler: function (btn)
            {
                var form = btn.up('form').getForm();
                if (form.isValid()) {
                    form.standardSubmit = true;
                    form.submit({
                        url: LABKEY.ActionURL.buildURL('filetransfer', 'configuration.view'),
                        method: 'POST',
                        scope: this
                    });
                }
            }
        });

        Ext4.create('Ext.form.Panel', {
            border : false,
            renderTo : 'SQVPicker',
            cls: 'configFormPanel',
            width: 520,
            items : [
                containingFilesHeader,
                endpointField,
                referenceListHeader,
                containerIdTextField,
                containerComboField,
                schemaComboField,
                queryComboField,
                columnComboField,
                transferSourceHeader,
                sourceEndpointDirField,
                { xtype: 'hidden', name: 'X-LABKEY-CSRF', value: LABKEY.CSRF }
            ],
            buttons: [
                cancelButton,
                saveButton
            ]
        });
    });
</script>