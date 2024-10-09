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

    @Override
    public boolean equals(Object obj) {
        VerbAction vrbAc = (VerbAction)obj;
        if (vrbAc.getVerb().equals(verb)) {
            return true;
        }
        if (vrbAc.getMethod() == method){
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return 2409;
    }
}
