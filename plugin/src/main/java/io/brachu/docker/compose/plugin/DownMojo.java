package io.brachu.docker.compose.plugin;

import io.brachu.johann.DockerCompose;
import io.brachu.johann.DownConfig;
import io.brachu.johann.exception.JohannException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "down", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class DownMojo extends AbstractDockerComposeMojo {

    /**
     * Should 'down' goal remove named volumes declared in the `volumes` section of the Compose file and anonymous volumes attached to containers?
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean removeVolumes;

    /**
     * Should 'down' goal remove containers for services not defined in the Compose file?
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean removeOrphans;

    /**
     * Specifies a shutdown timeout in seconds for 'down' goal.
     */
    @Parameter(required = true, defaultValue = "10")
    private int downTimeoutSeconds;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DockerCompose compose = dockerCompose();

        clearProperties(compose);
        down(compose);
    }

    private void down(DockerCompose compose) throws MojoExecutionException {
        try {
            compose.down(config());
        } catch (JohannException ex) {
            throw new MojoExecutionException("Docker-compose cluster failed to shut down", ex);
        }
    }

    private DownConfig config() {
        return DownConfig.defaults()
                .withRemoveVolumes(removeVolumes)
                .withRemoveOrphans(removeOrphans)
                .withTimeoutSeconds(downTimeoutSeconds);
    }

    private void clearProperties(DockerCompose compose) {
        clearProjectProperties();
        clearFailsafeArgLine(compose);
        clearSystemProperties();
    }

    private void clearProjectProperties() {
        project.getProperties().remove(PROJECT_NAME_PROPERTY);
    }

    private void clearFailsafeArgLine(DockerCompose compose) {
        String failsafeArgLine = project.getProperties().getProperty(FAILSAFE_ARGLINE_PROPERTY);
        if (failsafeArgLine != null) {
            failsafeArgLine = StringUtils.trimToNull(failsafeArgLine.replace(constructFailsafeArgLine(compose), ""));
            if (failsafeArgLine != null) {
                project.getProperties().setProperty(FAILSAFE_ARGLINE_PROPERTY, failsafeArgLine);
            } else {
                project.getProperties().remove(FAILSAFE_ARGLINE_PROPERTY);
            }
        }
    }

    private void clearSystemProperties() {
        System.clearProperty(PROJECT_NAME_PROPERTY);
    }

}
