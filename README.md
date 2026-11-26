# Hajz-App

Hajz-App est une application Android destinée à gérer les ateliers des clubs, les inscriptions des étudiants et les listes d’attente.

## Fonctionnalités

- Réservation dans les ateliers disponibles
- File d’attente FIFO avec promotion automatique
- Annulation avec Snackbar « ANNULER »
- Recherche par atelier ou club et filtres par club
- Vue des ateliers en `RecyclerView`
- Vue des réservations en `ListView`
- Indication des places libres, du texte « Complet » et des ateliers « Terminé »
- États vides pour les ateliers et réservations
- Mode administrateur protégé par un PIN
- Liste des participants confirmés puis de la liste d’attente
- Modification de capacité avec seuil égal au nombre de confirmés
- Suppression d’un atelier et de ses réservations
- Export texte partagé avec `Intent.ACTION_SEND`
- Thèmes clair et sombre Material 3
- Design system partagé : palette bleu/gold, espacements et rayons centralisés, boutons avec cibles tactiles de 48dp
- Dialogues Material et animations respectant l’échelle d’animation système
- États vides, descriptions d’images et contrôles accessibles
- Conservation complète de l’état lors d’une rotation ou d’un changement de thème
- Détails de session avec nom de l’instructeur, salle, date, capacité et places restantes
- Liste officielle de salles avec sélection par menu déroulant
- Édition administrateur du club, du titre, de la date, de la salle et de l’instructeur
- Recherche de sessions par instructeur ou par salle
- Interdiction de deux ateliers dans la même salle à la même date

## Gestion des places

Les réservations sont confirmées tant qu’une place est disponible. Lorsqu’un atelier est plein, les nouvelles réservations reçoivent un ticket d’attente croissant. Une annulation ou une augmentation de capacité promeut le plus ancien ticket FIFO.

## Contraintes

- Données en mémoire RAM uniquement
- Aucun réseau, aucune base de données et aucune persistance disque
- Les données sont perdues à la fermeture complète de l’application
- Le PIN administrateur est codé en dur
- Swipe-to-cancel n’est pas implémenté

## Technologie

- Kotlin
- Gradle
- Android Material 3
- JUnit 4

## Structure

- `app/src/main/java/tn/issatso/clubs/BookingManager.kt` : logique métier et états mémoire
- `StateSerializer.kt` : sérialisation pure Kotlin des données et de l’interface
- `ExportAteliers.kt` : génération pure du texte d’export
- `MainActivity.kt` : interface Android et interactions
- `app/src/main/res/layout/` : dispositions XML
- `app/src/main/res/values/` et `values-night/` : couleurs, chaînes et thèmes
- `app/src/test/` : tests JUnit

## Compilation et exécution

```bash
./gradlew test
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

PIN administrateur : `2026`

## Captures

- [Vue A claire](docs/screenshots/vueA_clair.png)
- [Vue B](docs/screenshots/vueB.png)
- [Vue C sombre](docs/screenshots/vueC_sombre.png)
- [Vue A sombre](docs/screenshots/vueA_sombre.png)
- [Mode administrateur](docs/screenshots/admin_mode.png)

## Résumé des tests

Les tests unitaires JUnit couvrent les réservations, la FIFO, les annulations, les capacités, les exports, les suppressions, les dates, les détails de session, les conflits de salle et la sérialisation d’état. Le rapport détaillé T1 à T31 est disponible dans [RAPPORT_TESTS.md](RAPPORT_TESTS.md).

La validation effectuée comprend `./gradlew testDebugUnitTest`, `assembleDebug`, `lintDebug` (0 erreur), un audit XML des dimensions (0 vue manquante), la preuve de lancement ADB et un monkey test de 3000 événements. Les vérifications TalkBack, contraste complet, 320dp, double-appui et captures avec données de démonstration restent manuelles.

## Limites connues

- Swipe-to-cancel est volontairement absent
- La restauration dépend du `Bundle` Android pendant une recréation, pas d’un stockage permanent
- Le PIN est fixe à des fins de démonstration

## Déclaration des sources

Les assistants IA suivants ont été utilisés sous supervision humaine : Gemini, GitHub Copilot et Claude.
