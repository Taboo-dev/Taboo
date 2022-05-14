package xyz.chalky.taboo;

import javax.security.auth.login.LoginException;

public class Launcher {

    public static void main(String[] args) {
        Thread.currentThread().setName("Taboo Main Thread");
        try {
            new Taboo();
        } catch (Exception e) {
            if (e instanceof LoginException) {
                Taboo.getLogger().error("Failed to login to Discord!", e);
			   // TODO: music buttons, bookmark, music bookmark and autocomplete, play pause embed ses
            } else {
                Taboo.getLogger().error("Failed to start Taboo!", e);
            }
        }
    }

}
