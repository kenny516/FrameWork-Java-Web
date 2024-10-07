package Utils;

import java.lang.reflect.Method;

public class VerbAction {
    protected String verb;
    protected Method method;

    public VerbAction(String verb, Method method) {
        this.setVerb(verb);
        this.setMethod(method);
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
