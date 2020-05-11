package org.hibersap.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.hibersap.configuration.AnnotationConfiguration;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.generation.bapi.ReverseBapiMapper;
import org.hibersap.generator.manager.ConnectionPropertiesManager;
import org.hibersap.generator.sap.SAPEntity;
import org.hibersap.generator.sap.SAPEntityBuilder;
import org.hibersap.generator.sap.SAPFunctionModuleSearch;
import org.hibersap.generator.util.FilterCollection;
import org.hibersap.generator.util.Utils;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.session.Session;
import org.hibersap.session.SessionManager;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An example Maven Mojo that resolves the current project's git revision and adds
 * that a new {@code exampleVersion} property to the current Maven project.
 * see: https://dzone.com/articles/tutorial-create-a-maven-plugin
 */
@Mojo(name = "generateSapEntities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateEntitiesMojo extends AbstractMojo {


    @Parameter(property = "namePattern", required = true)
    private String namePattern;

    @Parameter(property = "maxResults", required = false, defaultValue = "20")
    private Integer maxResults;

    @Parameter(property = "javaPackage", required = false, defaultValue = "org.hibersap.model")
    private String javaPackage;

    @Parameter(property = "outputDir", required = true)
    private String outputDir;


    @Parameter(property = "connectionProperties", required = true)
    private String connectionProperties;

    private PrintStream shell = System.out;


    public void execute() throws MojoExecutionException, MojoFailureException {
        ConnectionPropertiesManager connectionPropertiesManager;
        try {
            connectionPropertiesManager = new ConnectionPropertiesManager(this.connectionProperties);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage());
        }

        // The logic of our plugin will go here
        final SessionManagerConfig sessionManagerConfig = createSessionManagerConfig(connectionPropertiesManager);
        final AnnotationConfiguration configuration = new AnnotationConfiguration(sessionManagerConfig);
        final SessionManager sessionManager = configuration.buildSessionManager();
        String  namePattern = this.namePattern;
        ArrayList<SAPFunctionModuleSearch> searches = new ArrayList<SAPFunctionModuleSearch>();
        if(namePattern.contains(",")){
             // in this comma separated lsit was given
            String[] bapis =namePattern.split(",");
            for (int i = 0; i < bapis.length; i++) {
                searches.add(new SAPFunctionModuleSearch(bapis[i].trim(), maxResults));
            }
        }else{
            searches.add(new SAPFunctionModuleSearch(namePattern, maxResults));
        }

        final Session session = sessionManager.openSession();

        try {
            searches.forEach( functionModuleSearch ->session.execute(functionModuleSearch));

        } catch(Exception ex){
            throw new MojoExecutionException(ex.getMessage());
        }finally {
            session.close();
        }
        for (SAPFunctionModuleSearch functionModuleSearch : searches)
        {
            processSearch(sessionManager, functionModuleSearch);
        }



    }

    private void processSearch(SessionManager sessionManager, SAPFunctionModuleSearch functionModuleSearch) throws MojoExecutionException {
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

            try {
                File targetFileDir = createPackageDir();
                for (final JavaClassSource javaClass : javaClasses) {

                    saveJavaSource(targetFileDir, javaClass);


                }
            } catch (IOException ex) {
                throw new MojoExecutionException(ex.getMessage());
            }
        }
    }


    /**
     * Creates the necessary session manager configuration for the function module search
     *
     * @return the session manager configuration
     */
    protected SessionManagerConfig createSessionManagerConfig(ConnectionPropertiesManager sapConnectionPropertiesManager) {

        final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();

        sessionManagerConfig.setName(sapConnectionPropertiesManager.getSAPProperty("session-manager.name"));
        // Setting JCo context is not necessary, because it's set by default when creating a new SessionManangerConfig object
        sessionManagerConfig.addAnnotatedClass(SAPFunctionModuleSearch.class);

        //Filter JCo properties from property list
        //New Set necessary, because the sapConnection properties shall not be affected
        final Set<Map.Entry<Object, Object>> jcoConnectionProperties = new HashSet<Map.Entry<Object, Object>>(
                sapConnectionPropertiesManager.getAllSAPProperties());
        final FilterCollection filter = new FilterCollection(jcoConnectionProperties, "jco", "context");

        filter.filter();

        for (final Map.Entry<Object, Object> entry : jcoConnectionProperties) {
            sessionManagerConfig.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }

        return sessionManagerConfig;
    }

    private File createPackageDir() throws IOException {
        Path targetPath = Utils.package2Path(outputDir, javaPackage);
        File targetFileDir = targetPath.toFile();
        if (!targetFileDir.exists() && !targetFileDir.isDirectory()) {
            Files.createDirectories(targetPath);
            shell.println("Created package Directory [" + targetPath + "]");
        }
        return targetFileDir;
    }

    private void saveJavaSource(final File targetFileDir, final JavaClassSource javaClass) throws IOException {

        FileUtils.writeStringToFile(new File(targetFileDir, javaClass.getName() + ".java"), javaClass.toString(), "UTF-8");
        shell.println("Created SAP entity [" + javaClass.getQualifiedName() + "]");
    }


}