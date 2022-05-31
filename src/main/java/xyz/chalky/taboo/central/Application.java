package xyz.chalky.taboo.central;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import xyz.chalky.taboo.commands.misc.ShutdownCommand;
import xyz.chalky.taboo.util.ApplicationContextProvider;

@SpringBootApplication
@ComponentScan("xyz.chalky.taboo")
@EntityScan("xyz.chalky.taboo.database.model")
@EnableJpaRepositories("xyz.chalky.taboo.database.repository")
public class Application {

    private static Application instance;
    @Autowired private ApplicationContextProvider provider;

    public Application() {
        instance = this;
    }

    public static void main(String... args) {
        Thread.currentThread().setName("Taboo Main Thread");
        SpringApplication application = new SpringApplication(Application.class);
        application.setBanner((environment, sourceClass, out) -> {
            out.println(
                    """              
                    ___________     ___.                 \s
                    \\__    ___/____ \\_ |__   ____   ____ \s
                      |    |  \\__  \\ | __ \\ /  _ \\ /  _ \\\s
                      |    |   / __ \\| \\_\\ (  <_> |  <_> )
                      |____|  (____  /___  /\\____/ \\____/\s
                                   \\/    \\/              \s
                    """);
        });
        application.addListeners((ApplicationListener<ContextClosedEvent>) event -> {
            Taboo.getInstance().getWebhookClient().send(ShutdownCommand.shutdownEmbed);
        });
        application.run(args);
    }

    public static Application getInstance() {
        return instance;
    }

    public ApplicationContextProvider getProvider() {
        return provider;
    }

}
