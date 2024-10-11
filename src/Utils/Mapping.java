package Utils;

import java.lang.reflect.Method;
import java.util.HashSet;

public class Mapping {
    protected String class_name;
    protected HashSet<VerbAction> verbActions;

    public Mapping() {
        verbActions = new HashSet<VerbAction>();
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public HashSet<VerbAction> getVerbActions() {
        return verbActions;
    }

    public boolean isInVerbActions(String verb,Method method) {
        for (VerbAction verbAction : verbActions) {
            if (verbAction.getVerb().equals(verb)) {
                return true;
            }
            if (verbAction.getMethod() == method){
                return true;
            }
        }
        return false;
    }

    public void setVerbActions(HashSet<VerbAction> verbActions) {
        this.verbActions = verbActions;
    }
}


