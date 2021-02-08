package de.intranda.goobi.plugins;

import java.io.IOException;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Log4j2
public class SingleImageExportPlugin implements IExportPlugin, IPlugin {

    @Getter
    private String title = "intranda_export_singleImage";
    @Getter
    private PluginType type = PluginType.Validation;
    @Getter
    @Setter
    private Step step;

    @Setter
    private boolean testMode = false;


    @Override
    public List<String> getProblems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExportFulltext(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setExportImages(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean startExport(Process arg0) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
    WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
    TypeNotAllowedForParentException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startExport(Process arg0, String arg1) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
    WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
    TypeNotAllowedForParentException {
        // TODO Auto-generated method stub
        return false;
    }

}