package org.labkey.test.pages.filetransfer;

import org.labkey.test.Locator;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.util.LabKeyExpectedConditions;
import org.openqa.selenium.internal.WrapsDriver;

/**
 * Created by xingyang on 2/10/17.
 */
public class FileTransferConfigPage extends LabKeyPage
{
    public static Locator.XPathLocator endpointPathInput = Locator.xpath("//input[@name='endpointPath']");
    public static Locator.XPathLocator folderInput = Locator.xpath("//input[@name='lookupContainer']");
    public static Locator.XPathLocator tableInput = Locator.xpath("//input[@name='queryName']");
    public static Locator.XPathLocator fieldInput = Locator.xpath("//input[@name='columnName']");
    public static Locator.XPathLocator sourceEndpointDirInput = Locator.xpath("//input[@name='sourceEndpointDir']");

    public FileTransferConfigPage(WrapsDriver test)
    {
        super(test);
    }

    public void saveConfig(String path, String folder, String table, String field)
    {
        setEndpointPath(path);
        setReferenceListFolder(folder);
        setReferenceListTable(table);
        setReferenceListField(field);
        save();
    }

    public void setEndpointPath(String endpointPath)
    {
        waitForElement(endpointPathInput);
        setFormElement(endpointPathInput, endpointPath);
    }

    public void setReferenceListFolder(String referenceListFolder)
    {
        waitForElement(Locator.css(".containers-loaded-marker"));
        _ext4Helper.selectComboBoxItem("Folder:", referenceListFolder);
    }

    public void setReferenceListTable(String table)
    {
        waitForElement(Locator.css(".query-loaded-marker"));
        shortWait().until(LabKeyExpectedConditions.elementIsEnabled(tableInput));
        _ext4Helper.selectComboBoxItem("List:", table);
    }

    public void setReferenceListField(String column)
    {
        waitForElement(Locator.css(".column-loaded-marker"));
        shortWait().until(LabKeyExpectedConditions.elementIsEnabled(fieldInput));
        _ext4Helper.selectComboBoxItem("File Name Field:", column);
    }

    public void setSourceEndpointDir(String sourceEndpointDir)
    {
        waitForElement(sourceEndpointDirInput);
        setFormElement(sourceEndpointDirInput, sourceEndpointDir);
    }

    public void save()
    {
        clickButton("Save");
    }

    public String getEndpointPath()
    {
        waitForElement(endpointPathInput);
        return getFormElement(endpointPathInput);
    }

    public String getReferenceListFolder()
    {
        waitForElement(Locator.css(".containers-loaded-marker"));
        waitForElement(folderInput);
        return getFormElement(folderInput);
    }

    public String getTable()
    {
        waitForElement(Locator.css(".query-loaded-marker"));
        waitForElement(tableInput);
        return getFormElement(tableInput);
    }

    public String getField()
    {
        waitForElement(Locator.css(".column-loaded-marker"));
        waitForElement(fieldInput);
        return getFormElement(fieldInput);
    }

    public String getSourceEndpointDir()
    {
        waitForElement(sourceEndpointDirInput);
        return getFormElement(sourceEndpointDirInput);
    }
}
