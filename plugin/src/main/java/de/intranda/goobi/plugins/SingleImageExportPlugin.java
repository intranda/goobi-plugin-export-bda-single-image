package de.intranda.goobi.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
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
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
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
    public boolean startExport(Process process, String destination) throws IOException, InterruptedException, DocStructHasNoTypeException,
    PreferencesException, WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
    SwapException, DAOException, TypeNotAllowedForParentException {
        problems = new ArrayList<>();

        Prefs prefs = process.getRegelsatz().getPreferences();
        DocStructType physicalType = prefs.getDocStrctTypeByName("BoundBook");
        DocStructType pageType = prefs.getDocStrctTypeByName("page");
        MetadataType pathimagefilesType = prefs.getMetadataTypeByName("pathimagefiles");
        MetadataType identifierType = prefs.getMetadataTypeByName("CatalogIDDigital");
        // read mets file

        Fileformat ff = null;
        DocStruct mainElement = null;
        VariableReplacer replacer = null;
        try {
            ff = process.readMetadataFile();
            DigitalDocument dd = ff.getDigitalDocument();
            mainElement = dd.getLogicalDocStruct();
            replacer = new VariableReplacer(dd, prefs, process, null);
        } catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            problems.add("Cannot read metadata file.");
            return false;
        }

        List<Metadata> mainMetadata = mainElement.getAllMetadata();

        // read child elements
        List<DocStruct> children = mainElement.getAllChildren();

        if (children == null || children.isEmpty()) {
            problems.add("No photographs found");
            return false;
        }

        // create a new mets file for each element
        for (DocStruct picture : children) {

            MetsModsImportExport mm = new MetsModsImportExport(prefs);

            DigitalDocument dd = new DigitalDocument();
            mm.setDigitalDocument(dd);

            DocStruct dosctruct = dd.createDocStruct(picture.getType());
            dd.setLogicalDocStruct(dosctruct);

            // use generated shelfmark as identifier
            String photographIdentifier = null;
            for (Metadata md : picture.getAllMetadata()) {
                // copy metadata from photograph to new dosctruct
                if (md.getType().getName().equals("shelfmarksource")) {
                    photographIdentifier = md.getValue();
                }
                try {
                    Metadata newMd = new Metadata(md.getType());
                    newMd.setValue(md.getValue());
                    dosctruct.addMetadata(newMd);
                } catch (MetadataTypeNotAllowedException e) {
                    log.error(e);
                    problems.add("Metadata type is not allowed: " + md.getType());
                    return false;
                }
            }

            if (StringUtils.isBlank(photographIdentifier)) {
                problems.add("No shelfmark found, abort.");
                return false;
            }
            Metadata identifier = new Metadata(identifierType);
            identifier.setValue(photographIdentifier);
            dosctruct.addMetadata(identifier);

            for (Metadata md : mainMetadata) {
                // copy metadata to dosctruct, if the type doesn't exist
                List<? extends Metadata> mdl = dosctruct.getAllMetadataByType(md.getType());
                if (mdl == null || mdl.isEmpty()) {
                    try {
                        Metadata newMd = new Metadata(md.getType());
                        newMd.setValue(md.getValue());
                        dosctruct.addMetadata(newMd);
                    } catch (MetadataTypeNotAllowedException e) {
                        log.error(e);
                        problems.add("Metadata type is not allowed: " + md.getType());
                        return false;
                    }
                }
            }
            if (picture.getAllToReferences() == null || picture.getAllToReferences().isEmpty()) {
                problems.add("No image linked to photograph, abort.");
                return false;
            }
            // find image for the child element
            DocStruct image = picture.getAllToReferences().get(0).getTarget();
            String filename = Paths.get(image.getImageName()).getFileName().toString();

            // create new physical element

            DocStruct physical = dd.createDocStruct(physicalType);
            dd.setPhysicalDocStruct(physical);
            Metadata newmd = new Metadata(pathimagefilesType);
            newmd.setValue(process.getImagesTifDirectory(false));
            physical.addMetadata(newmd);

            DocStruct page = dd.createDocStruct(pageType);
            page.setImageName(filename);

            // physical order
            MetadataType mdt = prefs.getMetadataTypeByName("physPageNumber");
            Metadata mdTemp = new Metadata(mdt);
            mdTemp.setValue("1");
            page.addMetadata(mdTemp);

            // logical page no
            mdt = prefs.getMetadataTypeByName("logicalPageNumber");
            mdTemp = new Metadata(mdt);
            mdTemp.setValue("uncounted");

            // export image
            String exportFolder = replacer.replace(destination);
            if (process.getProjekt().isDmsImportCreateProcessFolder()) {
                exportFolder = Paths.get(exportFolder.toString(), photographIdentifier).toString();
            }
            Path path = Paths.get(exportFolder);

            if (!StorageProvider.getInstance().isFileExists(path)) {
                StorageProvider.getInstance().createDirectories(path);
            }
            Path sourceImageFile = Paths.get(process.getImagesTifDirectory(true), filename);
            Path destinationImageFolder = Paths.get(exportFolder, photographIdentifier);
            if (!StorageProvider.getInstance().isFileExists(destinationImageFolder)) {
                StorageProvider.getInstance().createDirectories(destinationImageFolder);
            }
            Path destinationImageFile = Paths.get(destinationImageFolder.toString(), filename);

            StorageProvider.getInstance().copyFile(sourceImageFile, destinationImageFile);
            // TODO error write error
            // write mets file
            String metsfile = Paths.get(exportFolder, photographIdentifier + ".xml").toString();
            if (!writeMetsFile(process, prefs, metsfile, mm)) {
                problems.add("Cannot write mets file, abort.");
                return false;
            }
        }
        return true;
    }

    private boolean writeMetsFile(Process process, Prefs prefs, String exportFolder, MetsModsImportExport mm) {
        try {
            VariableReplacer vp = new VariableReplacer(mm.getDigitalDocument(), prefs, process, null);

            List<ProjectFileGroup> myFilegroups = process.getProjekt().getFilegroups();

            if (myFilegroups != null && myFilegroups.size() > 0) {
                for (ProjectFileGroup pfg : myFilegroups) {

                    // check if source files exists
                    if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                        String foldername = process.getMethodFromName(pfg.getFolder());
                        if (foldername != null) {
                            Path folder = Paths.get(process.getMethodFromName(pfg.getFolder()));
                            if (folder != null && StorageProvider.getInstance().isFileExists(folder)
                                    && !StorageProvider.getInstance().list(folder.toString()).isEmpty()) {
                                VirtualFileGroup v = createFilegroup(vp, pfg);
                                mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                            }
                        }
                    } else {
                        VirtualFileGroup v = createFilegroup(vp, pfg);
                        mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                    }
                }
            }

            // Replace rights and digiprov entries.
            mm.setRightsOwner(vp.replace(process.getProjekt().getMetsRightsOwner()));
            mm.setRightsOwnerLogo(vp.replace(process.getProjekt().getMetsRightsOwnerLogo()));
            mm.setRightsOwnerSiteURL(vp.replace(process.getProjekt().getMetsRightsOwnerSite()));
            mm.setRightsOwnerContact(vp.replace(process.getProjekt().getMetsRightsOwnerMail()));
            mm.setDigiprovPresentation(vp.replace(process.getProjekt().getMetsDigiprovPresentation()));
            mm.setDigiprovReference(vp.replace(process.getProjekt().getMetsDigiprovReference()));
            mm.setDigiprovPresentationAnchor(vp.replace(process.getProjekt().getMetsDigiprovPresentationAnchor()));
            mm.setDigiprovReferenceAnchor(vp.replace(process.getProjekt().getMetsDigiprovReferenceAnchor()));

            mm.setMetsRightsLicense(vp.replace(process.getProjekt().getMetsRightsLicense()));
            mm.setMetsRightsSponsor(vp.replace(process.getProjekt().getMetsRightsSponsor()));
            mm.setMetsRightsSponsorLogo(vp.replace(process.getProjekt().getMetsRightsSponsorLogo()));
            mm.setMetsRightsSponsorSiteURL(vp.replace(process.getProjekt().getMetsRightsSponsorSiteURL()));

            mm.setPurlUrl(vp.replace(process.getProjekt().getMetsPurl()));
            mm.setContentIDs(vp.replace(process.getProjekt().getMetsContentIDs()));

            String pointer = process.getProjekt().getMetsPointerPath();
            pointer = vp.replace(pointer);
            mm.setMptrUrl(pointer);

            String anchor = process.getProjekt().getMetsPointerPathAnchor();
            pointer = vp.replace(anchor);
            mm.setMptrAnchorUrl(pointer);

            mm.setGoobiID(String.valueOf(process.getId()));
            mm.write(exportFolder);
        } catch (PreferencesException | WriteException e) {
            log.error(e);
            return false;
        }
        return true;
    }

    private VirtualFileGroup createFilegroup(VariableReplacer variableRplacer, ProjectFileGroup projectFileGroup) {
        VirtualFileGroup v = new VirtualFileGroup();
        v.setName(projectFileGroup.getName());
        v.setPathToFiles(variableRplacer.replace(projectFileGroup.getPath()));
        v.setMimetype(projectFileGroup.getMimetype());
        v.setFileSuffix(projectFileGroup.getSuffix());
        v.setFileExtensionsToIgnore(projectFileGroup.getIgnoreMimetypes());
        v.setIgnoreConfiguredMimetypeAndSuffix(projectFileGroup.isUseOriginalFiles());
        if (projectFileGroup.getName().equals("PRESENTATION")) {
            v.setMainGroup(true);
        }
        return v;
    }
}