package org.addon.hibersap;

import org.addon.hibersap.AbstractSAPUICommand;
import org.hibersap.configuration.AnnotationConfiguration;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.generator.sap.SAPEntity;
import org.hibersap.generator.sap.SAPEntityBuilder;
import org.hibersap.generator.sap.SAPFunctionModuleSearch;
import org.hibersap.generator.util.Utils;
import org.hibersap.generation.bapi.ReverseBapiMapper;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.session.Session;
import org.hibersap.session.SessionManager;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIPrompt;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.inject.Inject;

import java.util.List;

import java.util.Set;


public class GenerateSapEntitiesCommand extends AbstractSAPUICommand {


    @Inject
    private ProjectFactory projectFactory;




    @Inject
    @WithAttributes(label = "name-pattern", required = true, description = "Pattern to search SAP function names. Use * and ? as wildcards.")
    private UIInput<String> namePattern;

    @Inject
    @WithAttributes(label = "max-results", required = false, description = "Number of max. results. Use 0 for unlimited result list. Default value is 20", defaultValue = "20")
    private UIInput<Integer> maxResults;

    @Inject
    @WithAttributes(label = "package", required = false, description = "Package to use for hibersap class, defaults to org.hibersap.model", defaultValue = "org.hibersap.model")
    private UIInput<String> javaPackage;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
            builder.add(namePattern);
            builder.add(maxResults);

    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext) throws Exception {



        UIPrompt shell =uiExecutionContext.getPrompt();

        final SessionManagerConfig sessionManagerConfig = createSessionManagerConfig();
        final AnnotationConfiguration configuration = new AnnotationConfiguration( sessionManagerConfig );
        final SessionManager sessionManager = configuration.buildSessionManager();
        final SAPFunctionModuleSearch functionModuleSearch = new SAPFunctionModuleSearch( namePattern.getValue(), maxResults.getValue() );
        final Session session = sessionManager.openSession();

        try {
            session.execute( functionModuleSearch );
        } finally {
            session.close();
        }

        final List<String> functionNames = functionModuleSearch.getFunctionNames();
        final ReverseBapiMapper reverseBAPIMapper = new ReverseBapiMapper();
        final SAPEntityBuilder sapEntityBuilder = new SAPEntityBuilder();
        final JavaSourceFacet java = this.projectFactory.createTempProject().getFacet(JavaSourceFacet.class);


        functionNames.forEach(functionName -> {
            final BapiMapping functionMapping = reverseBAPIMapper.map( functionName, sessionManager );

            final String defaultClassName = Utils.toCamelCase( functionMapping.getBapiName(), '_' );



            sapEntityBuilder.createNew( defaultClassName, javaPackage.getValue(), functionMapping );
            final SAPEntity sapEntity = sapEntityBuilder.getSAPEntity();
            final Set<JavaClassSource> javaClasses = sapEntity.getStructureClasses();
            javaClasses.add( sapEntity.getBapiClass() );

            for ( final JavaClassSource javaClass : javaClasses ) {
                java.saveJavaSource( javaClass );
                shell.prompt( "Created SAP entity [" + javaClass.getQualifiedName() + "]" );
            }
        });






        return Results.success();
    }




    @Override
    protected String getDescription() {
        return "Generates the necessary Java classes for a given SAP function";
    }
}
