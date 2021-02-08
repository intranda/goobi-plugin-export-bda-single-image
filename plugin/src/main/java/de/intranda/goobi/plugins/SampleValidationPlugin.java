package de.intranda.goobi.plugins;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class SampleValidationPlugin implements IValidatorPlugin, IPlugin {

    @Getter
    private String title = "intranda_validation_sample";
    @Getter
    private PluginType type = PluginType.Validation;
    @Getter @Setter
    private Step step;

    @Setter
    private boolean testMode = false;

    private String value;
    
    /**
     * in case something needs to be prepared do it here
     */
    @Override
    public void initialize(Process inProcess) {
    }
    
    @Override
    public boolean validate() {
        boolean valid = false;

        // read parameters from correct block in configuration file
        SubnodeConfiguration myconfig = ConfigPlugins.getProjectAndStepConfig(title, step);
        value = myconfig.getString("value", "default value"); 

        // do a simple validation and pass back the result
        if (value.equals("valid")) {
            valid = true;
            log.debug("The result of the SampleValidationPlugin was: valid");
        } else {
            log.debug("The result of the SampleValidationPlugin was: not valid");
            Helper.setFehlerMeldung("The validation was not successful");
        }
        
        // return the validation result
        return valid;
    }

    /* *************************************************************** */
    /*                                                                 */
    /* the following methods are mostly not needed for typical imports */
    /*                                                                 */
    /* *************************************************************** */

    @Override
    public Step getStepObject() {
        return step;
    }

    @Override
    public void setStepObject(Step so) {
        this.step = so;
    }
    
}