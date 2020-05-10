package org.addon.hibersap;

import org.addon.hibersap.AbstractSAPUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIPrompt;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;


public class ListPropertiesCommand extends AbstractSAPUICommand {




    @Inject
    UIInput<String> input;


    @Override
    public void initializeUI(UIBuilder builder) throws Exception {

    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext) throws Exception {

        UIPrompt shell =uiExecutionContext.getPrompt();

        final Set<Map.Entry<Object, Object>> properties = this.sapConnectionPropertiesManager.getAllSAPProperties();//TODO sort entries

        for ( final Map.Entry<Object, Object> property : properties ) {

            shell.prompt( property.getKey() + "=" + property.getValue() );
        }
        return Results.success();
    }



    @Override
    protected String getDescription() {
        return "Lists all available connection properties";
    }
}
