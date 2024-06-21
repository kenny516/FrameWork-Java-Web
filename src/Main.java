import Model.ModelAndView;
import com.thoughtworks.paranamer.*;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception {
        Method method = ModelAndView.class.getMethod("setUrl", String.class);

        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(method);
        System.out.println("taille"+parameterNames.length);
        for (int i = 0; i < parameterNames.length; i++) {
            System.out.println("Parameter " + i + " name: " + parameterNames[i]);
        }
    }
}