@echo off

rem Chemin vers le dossier contenant les fichiers .class
set CLASS_DIR=D:\L3\s5\MrNaina\FrameWork-Java-Web\out\production\FrameWork-Java-Web

rem Nom du fichier JAR de destination (modifiable selon vos besoins)
set JAR_FILE=Framework.jar

rem Vérification si le dossier contenant les classes existe
if not exist "%CLASS_DIR%" (
    echo Le dossier spécifié n'existe pas.
    exit /b 1
)



rem Vérification si le fichier JAR de destination existe déjà
if exist "%JAR_FILE%" (
    echo Le fichier JAR de destination existe déjà. Veuillez supprimer le fichier existant avant de continuer.
    exit /b 1
)

rem Création du fichier JAR
echo Création du fichier JAR...
jar cf "%JAR_FILE%" -C "%CLASS_DIR%" .

echo Le fichier JAR a été créé avec succès.
pause
