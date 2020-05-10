package org.addon.hibersap.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.hibersap.configuration.AnnotationConfiguration;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.generation.bapi.ReverseBapiMapper;
import org.hibersap.generator.sap.SAPEntity;
import org.hibersap.generator.sap.SAPEntityBuilder;
import org.hibersap.generator.sap.SAPFunctionModuleSearch;
import org.hibersap.generator.util.Utils;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.session.Session;
import org.hibersap.session.SessionManager;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * An example Maven Mojo that resolves the current project's git revision and adds
 * that a new {@code exampleVersion} property to the current Maven project.
 * see: https://dzone.com/articles/tutorial-create-a-maven-plugin
 */
@Mojo(name = "generateSapEntities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateEntitiesMojo extends AbstractSAPMojo {




    @Parameter(property ="name-pattern", required = true)
    private String namePattern;

    @Parameter(property = "max-results", required = false,  defaultValue = "20")
    private Integer maxResults;

    @Parameter(property = "package", required = false,  defaultValue = "org.hibersap.model")
    private String javaPackage;

    @Parameter(property = "outputDir", required = true)
    private String outputDir;



    public void execute() throws MojoExecutionException, MojoFailureException {
        // The logic of our plugin will go here
        final SessionManagerConfig sessionManagerConfig = createSessionManagerConfig();
        final AnnotationConfiguration configuration = new AnnotationConfiguration( sessionManagerConfig );
        final SessionManager sessionManager = configuration.buildSessionManager();
        final SAPFunctionModuleSearch functionModuleSearch = new SAPFunctionModuleSearch( namePattern, maxResults );
        final Session session = sessionManager.openSession();

        try {
            session.execute( functionModuleSearch );
        } finally {
            session.close();
        }

        final List<String> functionNames = functionModuleSearch.getFunctionNames();
        final ReverseBapiMapper reverseBAPIMapper = new ReverseBapiMapper();
        final SAPEntityBuilder sapEntityBuilder = new SAPEntityBuilder();


        for (String functionName : functionNames) {
            final BapiMapping functionMapping = reverseBAPIMapper.map(functionName, sessionManager);

            final String defaultClassName = Utils.toCamelCase(functionMapping.getBapiName(), '_');


            sapEntityBuilder.createNew(defaultClassName, javaPackage, functionMapping);
            final SAPEntity sapEntity = sapEntityBuilder.getSAPEntity();
            final Set<JavaClassSource> javaClasses = sapEntity.getStructureClasses();
            javaClasses.add(sapEntity.getBapiClass());

            for (final JavaClassSource javaClass : javaClasses) {
                try {
                    saveJavaSource(javaClass);
                } catch (IOException ex) {
                    throw new MojoExecutionException(ex.getMessage());
                }

            }
        }


    }

    private void saveJavaSource(JavaClassSource javaClass) throws IOException {
        FileUtils.writeStringToFile(new File(outputDir,javaClass.getQualifiedName()), javaClass.toString(), "UTF-8");
        shell.println( "Created SAP entity [" + javaClass.getQualifiedName() + "]" );
    }


}