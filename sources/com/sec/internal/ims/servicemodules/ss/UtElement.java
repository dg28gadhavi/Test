package com.sec.internal.ims.servicemodules.ss;

/* compiled from: UtConfigData */
class UtElement {
    public static final String ELEMENT_CF = "communication-diversion";
    public static final String ELEMENT_CLI_NOT_RESTRICTED = "presentation-not-restricted";
    public static final String ELEMENT_CLI_RESTRICTED = "presentation-restricted";
    public static final String ELEMENT_CONDITION = "conditions";
    public static final String ELEMENT_CW = "communication-waiting";
    public static final String ELEMENT_DEFAULT_BEHAV = "default-behaviour";
    public static final String ELEMENT_ICB = "incoming-communication-barring";
    public static final String ELEMENT_IDENTITY = "identity";
    public static final String ELEMENT_OCB = "outgoing-communication-barring";
    public static final String ELEMENT_OIP = "originating-identity-presentation";
    public static final String ELEMENT_OIR = "originating-identity-presentation-restriction";
    public static final String ELEMENT_ONE = "one";
    public static final String ELEMENT_RULE = "rule";
    public static final String ELEMENT_RULE_DEACTIVATED = "rule-deactivated";
    public static final String ELEMENT_RULE_SET = "ruleset";
    public static final String PARSE_BEHAVIOUR = "//*[contains(local-name(), 'default-behaviour')]";
    public static final String PARSE_ERROR = "//*[local-name()='constraint-failure']";
    public static final String PARSE_NO_REPLY_TIMER = "//*[local-name()='NoReplyTimer']";
    public static final String PARSE_ROOT_ACTIVATION = "//@active";
    public static final String PARSE_ROOT_BARRING = "//*[contains(local-name(), 'barring')]";
    public static final String PARSE_RULE = "//*[local-name()='rule']";

    UtElement() {
    }
}
