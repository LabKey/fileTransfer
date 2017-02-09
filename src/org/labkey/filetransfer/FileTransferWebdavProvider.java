package org.labkey.filetransfer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.webdav.FileSystemResource;
import org.labkey.api.webdav.WebdavResolverImpl;
import org.labkey.api.webdav.WebdavResource;
import org.labkey.api.webdav.WebdavService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xingyang on 2/8/17.
 */
public class FileTransferWebdavProvider implements WebdavService.Provider
{
    public static final String FILE_LINK = "@filetransfer";

    @Nullable
    @Override
    public Set<String> addChildren(@NotNull WebdavResource target)
    {
        if (!(target instanceof WebdavResolverImpl.WebFolderResource))
            return null;
        WebdavResolverImpl.WebFolderResource folder = (WebdavResolverImpl.WebFolderResource) target;
        Container c = folder.getContainer();

        String path = FileTransferManager.get().getEndpointPath(c);
        if (null != path)
            return PageFlowUtil.set(FILE_LINK);

        return null;
    }

    @Override
    public WebdavResource resolve(@NotNull WebdavResource parent, @NotNull String name)
    {
        if (!FILE_LINK.equalsIgnoreCase(name))
            return null;
        if (!(parent instanceof WebdavResolverImpl.WebFolderResource))
            return null;
        WebdavResolverImpl.WebFolderResource folder = (WebdavResolverImpl.WebFolderResource) parent;
        Container c = folder.getContainer();
        if (null == c)
            return null;
        String path = FileTransferManager.get().getEndpointPath(c);
        if (null == path)
            return null;
        return new FileTransferFolderResource(folder, c, path);
    }

    private class FileTransferFolderResource extends FileSystemResource
    {
        Container c;

        FileTransferFolderResource(WebdavResource parent, Container c, String path)
        {
            super(parent.getPath(), FILE_LINK);

            this.c = c;
            _containerId = c.getId();

            _files = new ArrayList<>();
            _files.add(new FileInfo(new File(path)));

            setPolicy(c.getPolicy());
        }

        @Override
        public boolean canDelete(User user, boolean forDelete, List<String> msg)
        {
            return false;
        }

        @Override
        public boolean canCreate(User user, boolean forCreate)
        {
            return false;
        }

        @Override
        protected boolean hasAccess(User user) //TODO do we need folder permission to allow listing?
        {
            return user.isSiteAdmin() || c.getPolicy().getPermissions(user).size() > 0;
        }

        @Override
        public boolean canList(User user, boolean forRead)
        {
            return hasAccess(user);
        }

        @Override
        public String getName()
        {
            return FILE_LINK;
        }

        public FileSystemResource find(String name)
        {
            if (_files != null)
                return new FileSystemResource(this, name);
            return null;
        }
    }

}
