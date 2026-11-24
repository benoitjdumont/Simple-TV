# Simple TV

Lanceur d'accueil léger pour Android TV et Google TV. Pas de publicité, pas de recommandations, peu de mémoire.

## Fonctions

- Grille de tes applications, triées par nom.
- Heure, date et météo en haut à droite (Open-Meteo, sans clé).
- Nom et logo en haut à gauche.
- Fond d'écran au choix : aucun, une image, ou un diaporama d'un dossier (clé USB ou dossier local).
- Langue : celle de la box. Les traductions sont dans `app/src/main/res/values-*`.

## Installation de l'APK (sans Android Studio)

1. Dans l'onglet **Actions** du dépôt, ouvre le dernier build réussi.
2. Télécharge l'artefact `simple-tv-debug` et décompresse-le.
3. Installe l'APK avec ADB :
   ```
   adb connect ADRESSE_IP_DE_LA_BOX:PORT
   adb install -r app-debug.apk
   ```
4. Sur la box, choisis Simple TV comme application d'accueil si Android le demande.

## Ajouter une langue

Copie `app/src/main/res/values/strings.xml` dans `app/src/main/res/values-XX/strings.xml`, où `XX` est le code de la langue (par exemple `values-sv` pour le suédois), puis traduis les textes. Android choisit le bon fichier automatiquement.

## Limites

- Google Photos n'est pas inclus : il n'existe pas d'accès simple depuis une TV. Copie tes photos préférées dans un dossier et choisis-le dans les réglages.
- Le logo est un placeholder, à remplacer dans `app/src/main/res/drawable/ic_logo.xml`.
