# Rapport de tests — Hajz-App

| N° | Action/Données | Attendu | Observé | Statut |
|---|---|---|---|---|
| T1 | Réserver une place libre | Réservation confirmée | Test unitaire `reserveWithFreePlaceIsConfirmedAndDecreasesAvailability` | Réussi |
| T2 | Atelier complet | Ticket d’attente #1, #2... | Test unitaire `fullWorkshopAssignsIncreasingFifoTickets` | Réussi |
| T3 | Annuler une réservation confirmée | Promotion FIFO | Test unitaire `cancellingConfirmedReservationPromotesSmallestTicket` | Réussi |
| T4 | Annuler une réservation en attente | Aucun effet sur les autres positions | Test unitaire `cancellingWaitingReservationDoesNotPromoteAnotherReservation` | Réussi |
| T5 | Annuler puis appuyer sur « ANNULER » du Snackbar | Réservation et promotion restaurées | Test unitaire `undoCancellationRestoresReservationAndPromotionExactly` | Réussi |
| T6 | Même étudiant dans le même atelier | Inscription refusée | Test unitaire `duplicateReservationIsRejectedCaseInsensitively` | Réussi |
| T7 | Nom ou prénom vide | Champs obligatoires | Test unitaire `emptyNameIsRejected` | Réussi |
| T8 | Recherche par atelier ou club | Résultats filtrés | Test unitaire `FiltreAteliersTest` | Réussi |
| T9 | Filtre club | Seuls les ateliers du club sont affichés | Test unitaire `FiltreAteliersTest` | Réussi |
| T10 | Atelier passé | Carte « Terminé », bouton désactivé | Vérifié par le code et les tests de dates `dateRelationsPastTodayAndFutureAreCorrect` ; à confirmer à l’écran | À faire |
| T11 | Atelier passé dans Vue B | Atelier absent du Spinner | Vérifié par le filtrage du Spinner dans `MainActivity`; à confirmer à l’écran | À faire |
| T12 | Compteur de places plein | Texte « Complet », aucune place disponible | Test unitaire `fullWorkshopAssignsIncreasingFifoTickets` | Réussi |
| T13 | Vue C avec liste et liste vide | Réservations dans le filtre ou état vide lisible | `RecyclerView` vérifiée par compilation ; à confirmer à l’écran | À faire |
| T14 | Augmenter la capacité d’un atelier ayant une attente | Promouvoir les tickets FIFO et afficher le nombre | Tests unitaires `increasingCapacityPromotesWaitingReservationsInOrder` et `capacityCannotDropBelowConfirmedReservations` ; contrôle ADB manuel requis | À faire |
| T15 | Rotation : Vue, filtre, recherche, chip, admin | Toutes les données et valeurs UI sont restaurées | Test unitaire `StateSerializerTest`; rotation ADB exécutée, PID resté actif | Réussi partiel |
| T16 | Un étudiant réserve et voit SA réservation dans Vue C | Visible | À vérifier | À vérifier |
| T17 | Un étudiant tente de swiper une réservation d'un autre | Bloqué, Toast | À vérifier | À vérifier |
| T18 | Admin connecté ouvre Vue C | Voit toutes les réservations | À vérifier | À vérifier |
| T19 | Admin swipe une réservation d'un étudiant | Annulée + promotion FIFO | À vérifier | À vérifier |
| T20 | Ouvrir l’application sans session sauvegardée | Écran de connexion affiché | À vérifier manuellement sur téléphone | À faire |
| T21 | Login avec identifiants corrects (`etudiant1`) | Accès Vue A, statut « Connecté : Ali Ben Salah » | À vérifier manuellement sur téléphone | À faire |
| T22 | Login avec mauvais mot de passe | Toast « Identifiants incorrects », pas d’accès | À vérifier manuellement sur téléphone | À faire |
| T23 | Logout | Retour à l’écran de login, sessions et vues réinitialisées | À vérifier manuellement sur téléphone | À faire |
| T24 | Login admin | Accès Vue A + statut « Mode administrateur » | À vérifier manuellement sur téléphone | À faire |
| T25 | Un étudiant tente de voir les réservations d'un autre | Vue C vide ou seulement les siennes | À vérifier manuellement sur téléphone | À faire |
| T26 | Admin voit toutes les réservations | Liste complète | À vérifier manuellement sur téléphone | À faire |
| T27 | Un étudiant tente de swiper la réservation d'un autre | Bloqué (invisible dans sa Vue C) | À vérifier manuellement sur téléphone | À faire |
| T28 | Admin swipe n'importe quelle réservation | Fonctionne, promotion FIFO | À vérifier manuellement sur téléphone | À faire |

Tests manuels encore nécessaires : T10, T11, T13, T14, T17 à T28. T15 nécessite aussi une vérification visuelle complète de toutes les valeurs restaurées.

| N° | Action/Données | Attendu | Observé | Statut |
|---|---|---|---|---|
| T29 | Appuyer sur « Détails » sur un atelier | Dialogue avec titre, club, date, salle, instructeur, places | À vérifier manuellement sur téléphone | À faire |
| T30 | Admin ajoute un atelier avec instructeur et salle | Atelier ajouté ; Détails affiche les informations | À vérifier manuellement sur téléphone | À faire |
| T31 | Ajout sans instructeur ou sans salle | Toast d’erreur ; aucun atelier ajouté | À vérifier manuellement sur téléphone | À faire |
| T32 | Admin édite date, salle, instructeur, titre et club d’un atelier réservé | Détails actualisés, réservations conservées, chips actualisés | Test unitaire `editDetailsSuccessPreservesReservationsAndNoFalseSelfDuplicate`; contrôle visuel ADB requis | À faire |
| T33 | Ajout ou édition dans une salle déjà utilisée à cette date | « Salle déjà occupée à cette date » | Tests unitaires `addWorkshopRejectsRoomConflictAndAddsValidSession` et `editDetailsRejectsRoomConflictButAllowsNonConflictAndPastDate` | Réussi |
| T34 | Recherche par nom d’instructeur ou par salle | Atelier correspondant affiché | Tests unitaires `searchMatchesInstructorAndRoom` | Réussi |
| T35 | Exporter les ateliers | Instructeur et salle présents dans le texte | Tests unitaires `exportStartsWithAppNameAndContainsWorkshopDetails` | Réussi |
| T36 | Consulter Vue C | Chaque réservation affiche « Salle : I05 » ou la salle correspondante | À vérifier manuellement sur téléphone | À faire |

| T37 | Dialogues d’ajout, édition, suppression, annulation et détails en clair/sombre | Apparence cohérente, erreurs inline et dialogue conservé en cas d’erreur | MaterialAlertDialogBuilder est utilisé ; vérification visuelle et erreurs inline à faire | À faire |
| T38 | Transitions, insertions/suppressions, promotion et animations désactivées | Animations fluides et ignorées quand l’échelle système vaut 0 | Code compilé ; test visuel et réglage système à faire | À faire |
| T39 | Échelle de police 200 % et grande taille d’affichage | Aucun texte critique tronqué ou superposé | `uiautomator dump` exécuté à `font_scale=2.0`; validation visuelle à faire | À faire |
| T40 | Parcours TalkBack | Ordre de focus, résumés, actions et annonces accessibles | À vérifier manuellement sur téléphone | À faire |
| T41 | Paysage et écran 320dp | Écran utilisable sans chevauchement | Non exécuté sur un profil 320dp dédié | À faire |
| T42 | Monkey 3000 événements | Aucun crash ni ANR | `adb shell monkey -p tn.issatso.clubs --throttle 100 --ignore-crashes -v 3000` : 3000 événements, `Monkey finished`, PID `18999`, aucune erreur FATAL/ANR | Réussi |
| T43 | Double appui rapide sur Réserver, Confirmer, Annuler, Ajouter, Supprimer et Exporter | Une seule action métier | Non exécuté | À faire |
| T44 | Contraste AA dans les deux thèmes | Ratios conformes WCAG AA | Palette auditée par conception ; tableau de ratios complet non exécuté | À faire |
