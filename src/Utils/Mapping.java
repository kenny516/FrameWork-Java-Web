package Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Mapping {
    protected String class_name;
    protected List<VerbAction> verbActions;

    public Mapping() {
        verbActions = new ArrayList<VerbAction>();
    }

    public Mapping(String class_name) {
        verbActions = new ArrayList<VerbAction>();
        this.setClass_name(class_name);
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public List<VerbAction> getVerbActions() {
        return verbActions;
    }

    public boolean isInVerbActions(String verb) {
        for (VerbAction verbAction : verbActions) {
            if (verbAction.getVerb().equals(verb)) {
                return true;
            }
        }
        return false;
    }

    public void setVerbActions(ArrayList<VerbAction> verbActions) {
        this.verbActions = verbActions;
    }
}


