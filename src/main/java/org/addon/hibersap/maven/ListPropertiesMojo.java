package org.addon.hibersap.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.Map;
import java.util.Set;

/**
 * An example Maven Mojo that resolves the current project's git revision and adds
 * that a new {@code exampleVersion} property to the current Maven project.
 * see: https://dzone.com/articles/tutorial-create-a-maven-plugin
 */
@Mojo(name = "listProperites", defaultPhase = LifecyclePhase.INITIALIZE)
public class ListPropertiesMojo extends AbstractSAPMojo {



    @Parameter(property = "project", readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // The logic of our plugin will go here


        final Set<Map.Entry<Object, Object>> properties = this.sapConnectionPropertiesManager.getAllSAPProperties();//TODO sort entries

        for ( final Map.Entry<Object, Object> property : properties ) {

            shell.println( property.getKey() + "=" + property.getValue() );
        }
    }
}