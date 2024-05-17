package Utils;

import Annotation.Controller;


import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AccesController {

    public static ArrayList<Class<?>> getControllerList(String package_class,String directoryPath) {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        File b = new File(directoryPath);
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
