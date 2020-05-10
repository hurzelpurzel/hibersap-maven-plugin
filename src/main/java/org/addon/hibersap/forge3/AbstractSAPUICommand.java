package org.addon.hibersap.forge3;

import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.generator.manager.ConnectionPropertiesManager;
import org.hibersap.generator.sap.SAPFunctionModuleSearch;
import org.hibersap.generator.util.FilterCollection;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * See : https://planet.jboss.org/post/addon_development_with_forge_2_a_basic_primer
 */
public abstract class AbstractSAPUICommand extends AbstractUICommand {


    /**
     * The SAP connection properties *
     */
    @Inject
    protected  ConnectionPropertiesManager sapConnectionPropertiesManager;




    /**
     * Creates the necessary session manager configuration for the function module search
     *
     * @return the session manager configuration
     */
    protected SessionManagerConfig createSessionManagerConfig() {
        final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();

        sessionManagerConfig.setName( this.sapConnectionPropertiesManager.getSAPProperty( "session-manager.name" ) );
        // Setting JCo context is not necessary, because it's set by default when creating a new SessionManangerConfig object
        sessionManagerConfig.addAnnotatedClass( SAPFunctionModuleSearch.class );

        //Filter JCo properties from property list
        //New Set necessary, because the sapConnection properties shall not be affected
        final Set<Map.Entry<Object, Object>> jcoConnectionProperties = new HashSet<Map.Entry<Object, Object>>(
                this.sapConnectionPropertiesManager.getAllSAPProperties() );
        final FilterCollection filter = new FilterCollection( jcoConnectionProperties, "jco", "context" );

        filter.filter();

        for ( final Map.Entry<Object, Object> entry : jcoConnectionProperties ) {
            sessionManagerConfig.setProperty( entry.getKey().toString(), entry.getValue().toString() );
        }

        return sessionManagerConfig;
    }

    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass())
                .category(Categories.create("SAP"))
                .name(getClass().getName())
                .description(getDescription());
    }


    protected  abstract  String getDescription();
}
