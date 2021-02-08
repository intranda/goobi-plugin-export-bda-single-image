package de.intranda.goobi.plugins;

import java.io.IOException;
import java.util.ArrayList;
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
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsModsImportExport;

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

    @Getter
    private List<String> problems;

    @Override
    public void setExportFulltext(boolean arg0) {
    }

    @Override
    public void setExportImages(boolean arg0) {
    }

    @Override
    public boolean startExport(Process process) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
    WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
    TypeNotAllowedForParentException {
        String benutzerHome = process.getProjekt().getDmsImportImagesPath();

        return startExport(process, benutzerHome);
    }

    @Override
    public boolean startExport(Process process, String exportFolder) throws IOException, InterruptedException, DocStructHasNoTypeException,
    PreferencesException, WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
    SwapException, DAOException, TypeNotAllowedForParentException {

        Prefs prefs = process.getRegelsatz().getPreferences();
        DocStructType type = prefs.getDocStrctTypeByName("Picture");

        problems = new ArrayList<>();

        // read mets file

        Fileformat ff = process.readMetadataFile();

        DocStruct mainElement = ff.getDigitalDocument().getLogicalDocStruct();

        List<Metadata> mainMetadata = mainElement.getAllMetadata();

        // read child elements
        List<DocStruct> children = mainElement.getAllChildren();

        // create a new mets file for each element
        for (DocStruct picture : children) {


            MetsModsImportExport mm = new MetsModsImportExport(prefs);

            DigitalDocument dd = new DigitalDocument();
            mm.setDigitalDocument(dd);

            DocStruct dosctruct = dd.createDocStruct(type);

            for (Metadata md : picture.getAllMetadata()) {
                // copy metadata to dosctruct

            }
            for (Metadata md: mainMetadata) {
                // copy metadata to dosctruct, if the type doesn't exist
            }

            // find image for the child element
            DocStruct image = picture.getAllFromReferences().get(0).getSource();
            String filename = image.getImageName();

            // create new physical element

            // export image, write mets file

        }
        return true;
    }

}