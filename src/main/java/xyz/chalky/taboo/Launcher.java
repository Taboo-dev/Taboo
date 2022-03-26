package xyz.chalky.taboo;

import javax.security.auth.login.LoginException;

public class Launcher {

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        try {
            new Taboo();
        } catch (Exception e) {
            if (e instanceof LoginException) {
                Taboo.getLogger().error("Failed to login to Discord!", e);
            } else {
                Taboo.getLogger().error("Failed to start Taboo!", e);
            }
        }
    }

}
