package Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class AccesXML {

    public static String get_controller_class_path(String path_XML) {
        try {
            File file = new File(path_XML);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            // Récupération des éléments <controller>
            NodeList controllerList = doc.getElementsByTagName("controller");

            // Vérification si un élément <controller> est trouvé
            if (controllerList.getLength() > 0) {
                Node controllerNode = controllerList.item(0); // On suppose qu'il y a un seul élément <controller>
                if (controllerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element controllerElement = (Element) controllerNode;
                    return controllerElement.getTextContent().trim();
                }
            } else {
                System.out.println("Aucun élément <controller> trouvé dans le fichier web.xml.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Class<?>> find_classes(String directoryPath) throws IOException, ClassNotFoundException {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        // Vérification si le chemin spécifié est un répertoire
        if (directory.isDirectory()) {
            // Liste des fichiers dans le répertoire
            File[] files = directory.listFiles();

            if (files != null) {
                // Parcours des fichiers du répertoire
                for (File file : files) {
                    if (file.isFile()) {
                        // Vérification si le fichier est une classe
                        if (file.getName().endsWith(".class")) {
                            // Ajout de la classe à la liste
                            classFiles.add(loadClassFromFile(file));
                        }
                    }
                }
            }
        } else {
            System.out.println("Le chemin spécifié n'est pas un répertoire.");
        }

        return classFiles;
    }

    public static Class<?> loadClassFromFile(File file) throws IOException, ClassNotFoundException {
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
        String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
        return Class.forName(className, true, classLoader);
    }
}
