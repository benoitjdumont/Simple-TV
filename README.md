# Simple TV

Launcher Android TV léger, personnalisable et multilingue.

## Objectifs
- Écran d'accueil simple et rapide, pensé pour la télécommande.
- Applications favorites sélectionnées par l'utilisateur.
- Interface en français, anglais et espagnol (français par défaut).
- Accès aux réglages Android et au compte Google via les interfaces officielles du système.
- Vérification des versions publiées dans GitHub Releases et installation sur demande, avec les confirmations Android requises.
- Compatibilité visée : onn. 4K, appareils Android TV et Google TV compatibles avec les launchers tiers.

## Limites Android
Simple TV remplace l'écran d'accueil lorsqu'Android autorise les launchers tiers. Il ne désinstalle pas les applications système et ne peut pas contourner les restrictions du fabricant. L'utilisateur pourra toujours revenir au launcher d'origine depuis les réglages Android.

## Développement
Projet Android natif en Kotlin et Jetpack Compose.

## Compilation et mises à jour

Chaque modification poussée sur `main` déclenche une compilation de test (APK debug) dans l'onglet **Actions**. Cet APK sert aux essais; il ne doit pas être utilisé comme APK de mise à jour durable.

Les versions publiques sont publiées dans **GitHub Releases** lorsqu'une étiquette Git `v*` est poussée. Le workflow `.github/workflows/release.yml` fabrique un APK release signé et le joint automatiquement à la publication. L'application vérifie ensuite la dernière publication via l'API GitHub.

### Préparer la signature (une seule fois)

Une clé privée de signature est indispensable pour que les futures versions puissent mettre à jour l'application sans désinstallation. Créer une clé localement (ne jamais la publier ni l'envoyer dans une conversation) :

```powershell
keytool -genkeypair -v -keystore simple-tv-release.jks -alias simple-tv -keyalg RSA -keysize 2048 -validity 10000
```

Convertir le fichier en Base64 sous Windows :

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("simple-tv-release.jks")) | Set-Clipboard
```

Dans **GitHub → Settings → Secrets and variables → Actions**, créer ces quatre secrets :
- `ANDROID_KEYSTORE_BASE64` : contenu Base64 copié.
- `ANDROID_KEYSTORE_PASSWORD` : mot de passe du fichier JKS.
- `ANDROID_KEY_ALIAS` : `simple-tv` (ou l'alias choisi).
- `ANDROID_KEY_PASSWORD` : mot de passe de la clé.

Conserver une copie sécurisée du fichier JKS et de ses mots de passe. Si cette clé est perdue, Android ne pourra plus installer les mises à jour par-dessus la version signée avec cette clé.

### Publier une version

Avant chaque publication, augmenter `versionCode` et mettre à jour `versionName` dans `app/build.gradle.kts`. Ensuite pousser une étiquette, par exemple `v1.0.1`. Le workflow crée la Release et y ajoute l'APK signé.
