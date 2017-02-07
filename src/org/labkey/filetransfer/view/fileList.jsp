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
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("sqv");
    }
%>
<%
    Container c = getContainer();
    User user = getUser();
%>
<div id="SQVPicker"></div>


<script type="text/javascript">

    Ext4.onReady(function()
    {
        var sqvModel = Ext4.create('LABKEY.sqv.Model', {});

        var containerIdTextField = Ext4.create('Ext.form.field.Text', {
            name: 'containerId',
            hidden: true
        });

        var containerComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeContainerComboConfig({
            name: 'container',
            editable: false,
            width: 510,
            listeners: {
                select: function(combo) {
                    containerIdTextField.setValue(combo.getValue());
                }
            }
        }));

        var schemaComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeSchemaComboConfig({
            name: 'schemaName',
            forceSelection: true,
            width: 300
        }));

        var queryComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeQueryComboConfig({
            name: 'queryName',
            forceSelection: true,
            width: 300
        }));

        var columnComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeColumnComboConfig({
            name: 'columnName',
            fieldLabel: 'Name',
            forceSelection: true,
            width: 300
        }));

        Ext4.create('Ext.form.Panel', {
            border : false,
            renderTo : 'SQVPicker',
            items : [
                containerIdTextField,
                containerComboField,
                schemaComboField,
                queryComboField,
                columnComboField,
                { xtype: 'hidden', name: 'X-LABKEY-CSRF', value: LABKEY.CSRF }
            ]
        });
    });
</script>