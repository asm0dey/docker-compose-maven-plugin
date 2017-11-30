package io.brachu.docker.compose.plugin;

import io.brachu.johann.DockerCompose;
import io.brachu.johann.exception.JohannException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "up", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class UpMojo extends AbstractDockerComposeMojo {

    public UpMojo() {
        super(false);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Config config = getConfig();
        DockerCompose compose = dockerCompose(config);

        up(compose, config);
        fillProperties(compose);
    }

    private void up(DockerCompose compose, Config config) throws MojoExecutionException {
        WaitConfig wait = config.getWait();
        try {
            compose.up();
            compose.waitForCluster(wait.getValue(), wait.getUnit());
        } catch (JohannException ex) {
            throw new MojoExecutionException("Unexpected exception while starting docker-compose cluster", ex);
        }
    }

    private void fillProperties(DockerCompose compose) {
        fillProjectProperties(compose);
        fillSystemProperties(compose);
    }

    private void fillProjectProperties(DockerCompose compose) {
        project.getProperties().setProperty(PROJECT_NAME_PROPERTY, compose.getProjectName());
    }

    private void fillSystemProperties(DockerCompose compose) {
        System.setProperty(PROJECT_NAME_PROPERTY, compose.getProjectName());
    }

}