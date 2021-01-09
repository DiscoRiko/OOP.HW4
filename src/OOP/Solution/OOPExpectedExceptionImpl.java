package OOP.Solution;

import OOP.Provided.OOPExpectedException;

import java.util.*;

public class OOPExpectedExceptionImpl implements OOPExpectedException{
    Class<? extends Exception> exception = null;
    List<String> messages = new LinkedList<>();

    @Override
    public Class<? extends Exception> getExpectedException() {
        return exception;
    }

    @Override
    public OOPExpectedException expect(Class<? extends Exception> expected) {
        this.exception = expected;
        return this;
    }

    @Override
    public OOPExpectedException expectMessage(String msg) {
        this.messages.add(msg);
        return this;
    }

    @Override
    public boolean assertExpected(Exception e) {
        if (exception == null)
            return false;
        if (!exception.isInstance(e))
            return false;
        for (String message : messages) {
            if (!e.getMessage().contains(message))
                return false;
        }
        return true;
    }

    static public OOPExpectedExceptionImpl none() {
        return new OOPExpectedExceptionImpl();
    }
}
