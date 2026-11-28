# Hajz-App

Hajz-App est une application Android destinée à gérer les ateliers des clubs, les inscriptions des étudiants et les listes d’attente.

## Binôme

- **Fakher Neila** — ING-A3-GL-AL-04
- **Mohammed Amine Naimi** — ING-A3-GL-**03**

**Module :** Développement Mobile — ISSAT Sousse
**Enseignante :** Mme Safa Ezzehi
**Année universitaire :** 2026–2027

---

Hajz-App est une application Android destinée à gérer les ateliers des clubs, les inscriptions des étudiants et les listes d'attente.


## Fonctionnalités

- Réservation dans les ateliers disponibles
- File d’attente FIFO avec promotion automatique
- Annulation des réservations par balayage
- Recherche par atelier ou club et filtres par club
- Vue des ateliers en `RecyclerView`
- Vue des réservations en `RecyclerView`
- Swipe-to-cancel sur les réservations (glisser à gauche/droite pour annuler)
- Indication des places libres, du texte « Complet » et des ateliers « Terminé »
- États vides pour les ateliers et réservations
- Actions d’administration réservées au rôle `ADMIN`
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

- Données en RAM (perdues à la fermeture)

## Comptes de démonstration

- `etudiant1` / `etudiant1` — Ali Ben Salah (ETUDIANT)
- `etudiant2` / `etudiant2` — Sami Trabelsi (ETUDIANT)
- `admin` / `admin123` — Administrateur (ADMIN)

## Sécurité et authentification

- La connexion avec l’un des comptes de démonstration est obligatoire pour accéder aux vues.
- Les rôles `ETUDIANT` et `ADMIN` déterminent les droits d’accès.
- Chaque étudiant ne consulte et n’annule que ses propres réservations ; l’administrateur voit toutes les réservations.
- Les identifiants sont codés en dur uniquement pour la démonstration. Les mots de passe ne sont pas enregistrés.
- Le rôle `ADMIN` est vérifié lors de la connexion via `AuthManager` ; aucun code d’accès séparé n’est nécessaire.

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

## Captures

- [Vue A claire](docs/screenshots/vueA_clair.png)
- [Vue B](docs/screenshots/vueB.png)
- [Vue C sombre](docs/screenshots/vueC_sombre.png)
- [Vue A sombre](docs/screenshots/vueA_sombre.png)
- [Mode administrateur](docs/screenshots/admin_mode.png)

## Résumé des tests

Les tests unitaires JUnit couvrent les réservations, la FIFO, les annulations, les capacités, les exports, les suppressions, les dates, les détails de session, les conflits de salle et la sérialisation d’état. Le rapport détaillé T1 à T44 est disponible dans [RAPPORT_TESTS.md](RAPPORT_TESTS.md).

La validation effectuée comprend `./gradlew testDebugUnitTest`, `assembleDebug`, `lintDebug` (0 erreur), un audit XML des dimensions (0 vue manquante), la preuve de lancement ADB et un monkey test de 3000 événements. Les vérifications TalkBack, contraste complet, 320dp, double-appui et captures avec données de démonstration restent manuelles.

## Limites connues

- La restauration dépend du `Bundle` Android pendant une recréation, pas d’un stockage permanent

## Déclaration des sources

Projet réalisé en binôme par :
- Fakher Neila (ING-A3-GL-AL-04)
- Mohammed Amine Naimi (ING-A3-GL-03)

Assistants IA utilisés sous supervision humaine : Gemini, GitHub Copilot et Claude. Tout le code a été lu, testé et adapté .
