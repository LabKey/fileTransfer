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
package org.labkey.test.pages.filetransfer;

import org.labkey.test.Locator;
import org.labkey.test.components.ext4.ComboBox;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.selenium.LazyWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;

public class CustomizeFileTransferPage<EC extends CustomizeFileTransferPage.ElementCache> extends LabKeyPage<EC>
{
    public CustomizeFileTransferPage(WrapsDriver test)
    {
        super(test);
    }

    public CustomizeFileTransferPage setEndpointDirectory(String endPoint)
    {
        setFormElement(elementCache().sourceEndpointDir, endPoint);
        return this;
    }

    public String getEndPointDirectory()
    {
        return getFormElement(elementCache().sourceEndpointDir);
    }

    public CustomizeFileTransferPage setWebPartTitle(String webPartTitle)
    {
        setFormElement(elementCache().webpartTitle, webPartTitle);
        return this;
    }

    public String getWebPartTitle()
    {
        return getFormElement(elementCache().webpartTitle);
    }

    public CustomizeFileTransferPage setLocalDirectory(String localDirectory)
    {
        setFormElement(elementCache().localFilesDirectory, localDirectory);
        return this;
    }

    public String getLocalDirectory()
    {
        return getFormElement(elementCache().localFilesDirectory);
    }

    public CustomizeFileTransferPage setFileName(String fileName)
    {
        setFormElement(elementCache().fileName, fileName);
        return this;
    }

    public String getFileName()
    {
        return getFormElement(elementCache().fileName);
    }

    public CustomizeFileTransferPage setFolder(String folderName)
    {
        elementCache().folder.selectComboBoxItem(folderName);
        waitForElementToDisappear(Locator.xpath("//li[@role='option'][@class='x4-boundlist-item'][text()='" + folderName + "']"));
        return this;
    }

    public CustomizeFileTransferPage setList(String listName)
    {
        elementCache().list.selectComboBoxItem(listName);
        waitForElementToDisappear(Locator.xpath("//li[@role='option'][@class='x4-boundlist-item'][text()='" + listName + "']"));
        return this;
    }

    public CustomizeFileTransferPage setFileNameField(String fileNameField)
    {
        elementCache().fileNameField.selectComboBoxItem(fileNameField);
        waitForElementToDisappear(Locator.xpath("//li[@role='option'][@class='x4-boundlist-item'][text()='" + fileNameField + "']"));
        return this;
    }

    public void clickSave()
    {
        elementCache().saveButton.click();
    }

    public void clickCancel()
    {
        elementCache().cancelButton.click();
    }

    public String getErrorMessage()
    {
        String msg;
        try
        {
            msg = elementCache().configMessage.getText();
            msg = msg.substring("Files Directory".length()).replace("\n", "");
        }
        catch(NoSuchElementException nse)
        {
            msg = "";
        }

        return msg;
    }

    @Override
    protected EC newElementCache()
    {
        return (EC) new CustomizeFileTransferPage.ElementCache();
    }

    protected class ElementCache extends LabKeyPage.ElementCache
    {
        protected WebElement webpartTitle = new LazyWebElement(Locator.xpath("//input[@name='webpart.title']"), this);
        protected WebElement localFilesDirectory = new LazyWebElement(Locator.xpath("//input[@name='localFilesDirectory']"),this);
        protected WebElement sourceEndpointDir = new LazyWebElement(Locator.xpath("//input[@name='sourceEndpointDir']"),this);
        protected WebElement fileName = new LazyWebElement(Locator.xpath("//input[@name='fileNameColumn']"), this);
        protected WebElement saveButton = new LazyWebElement(Locator.xpath(".//a[contains(@class, 'x4-btn')]//span[contains(text(), 'Save')]/ancestor::a"),this);
        protected WebElement cancelButton = new LazyWebElement(Locator.xpath(".//a[contains(@class, 'x4-btn')]//span[contains(text(), 'Cancel')]/ancestor::a"),this);
        protected ComboBox folder = ComboBox.ComboBox(getDriver()).withLabelContaining("Folder").findWhenNeeded(this);
        protected ComboBox list = ComboBox.ComboBox(getDriver()).withLabelContaining("List").findWhenNeeded(this);
        protected ComboBox fileNameField = ComboBox.ComboBox(getDriver()).withLabelContaining("File Name Field").findWhenNeeded(this);
        protected WebElement configMessage = new LazyWebElement(Locator.xpath("//span[contains(text(), 'Files Directory')]/parent::label"), this);
    }

}
