package Utils;

import Annotation.Controller;
import jakarta.servlet.ServletException;


import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AccesController {

    public static ArrayList<Class<?>> getControllerList(String package_class, String directoryPath) throws ServletException {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        File b = new File(directoryPath);
        if (!b.exists()) {
            throw new ServletException("Directory of controller not found =>" + directoryPath);
        }
        if (Objects.requireNonNull(b.listFiles()).length > 0) {
            throw new ServletException("Liste de Controller vide =>" + directoryPath);
        }
        for (File onefile : Objects.requireNonNull(b.listFiles())) {
            if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(package_class + "." + onefile.getName().split(".class")[0]);
                    if (clazz.isAnnotationPresent(Controller.class))
                        classFiles.add(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classFiles;
    }
}
