package org.example.blockcalc.common;

/** A rule violation that is shown to the user. The message is an i18n key from messages*.properties. */
public class BusinessException extends RuntimeException {
    private final String messageKey;
    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
