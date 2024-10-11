package Utils;

import java.lang.reflect.Method;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // même objet
        if (obj == null || getClass() != obj.getClass()) return false; // null ou mauvais type

        VerbAction that = (VerbAction) obj;
        // Comparer uniquement le verbe
        return Objects.equals(verb, that.verb);
    }

    @Override
    public int hashCode() {
        return 2409;
    }
}
