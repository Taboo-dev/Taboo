package xyz.chalky.taboo.backend;

public enum CommandFlag {
    ALLOWED_IN_DMS(),
    DEVELOPER_ONLY(),
    AUTO_DELETE_MESSAGE(),
    DISABLED(),
    PRIVATE_COMMAND(),
    MODERATOR_ONLY(),
    MUST_BE_IN_VC(),
    MUST_BE_IN_SAME_VC()
}
