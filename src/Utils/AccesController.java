package Utils;

import Annotation.Controller;
import jakarta.servlet.ServletException;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccesController {

    public static ArrayList<Class<?>> getControllerList(String packageClass, String directoryPath) throws ServletException {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        File baseDirectory = new File(directoryPath);

        if (!baseDirectory.exists()) {
            throw new ServletException("Directory of controller not found => " + directoryPath);
        }

        if (Objects.requireNonNull(baseDirectory.listFiles()).length == 0) {
            throw new ServletException("Liste de Controller vide => " + directoryPath);
        }

        try {
            List<Path> classPaths = new ArrayList<>();

            // Parcours récursif des fichiers dans le répertoire
            Files.walk(baseDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(classPaths::add);

            for (Path classPath : classPaths) {
                String className = buildClassName(packageClass, baseDirectory, classPath.toFile());

                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        classFiles.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace(); // Vous pouvez logger l'erreur ici
                }
            }
        } catch (IOException e) {
            throw new ServletException("Erreur lors du parcours des fichiers : " + e.getMessage());
        }

        return classFiles;
    }

    /**
     * Construit le nom complet de la classe en fonction du package et du chemin du fichier.
     */
    private static String buildClassName(String basePackage, File baseDirectory, File classFile) {
        String relativePath = classFile.getAbsolutePath().substring(baseDirectory.getAbsolutePath().length() + 1);
        String className = relativePath.replace(File.separator, ".").replace(".class", "");

        // ✅ Si basePackage est vide, ne pas ajouter un point
        return basePackage.isEmpty() ? className : basePackage + (className.isEmpty() ? "" : "." + className);
    }


}
