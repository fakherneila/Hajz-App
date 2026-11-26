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
| T13 | Vue C avec liste et liste vide | Réservations dans le filtre ou état vide lisible | `ListView` conservée et vérifiée par compilation ; à confirmer à l’écran | À faire |
| T14 | Augmenter la capacité d’un atelier ayant une attente | Promouvoir les tickets FIFO et afficher le nombre | Tests unitaires `increasingCapacityPromotesWaitingReservationsInOrder` et `capacityCannotDropBelowConfirmedReservations` ; contrôle ADB manuel requis | À faire |
| T15 | Rotation : Vue, filtre, recherche, chip, admin | Toutes les données et valeurs UI sont restaurées | Test unitaire `StateSerializerTest`; rotation ADB exécutée, PID resté actif | Réussi partiel |
| T16 | Basculer en mode sombre | État conservé et thème sombre appliqué | `cmd uimode night yes/no` exécuté ; PID resté actif, aucune exception fatale | Réussi partiel |
| T17 | Donner un mauvais PIN | Message « PIN incorrect », mode admin inchangé | À vérifier manuellement sur téléphone | À faire |
| T18 | Supprimer un atelier avec réservations | Confirmation avec nombre, suppression totale | Tests unitaires `supprimerAtelierWithReservationsClearsThem` et `supprimerAtelierWithoutReservationsRemovesIt`; contrôle ADB manuel requis | À faire |
| T19 | Exporter en admin | Résumé Hajz-App en texte, ordre confirmé puis attente | Test unitaire `ExportAteliersTest`; contrôle du partage à l’écran requis | À faire |
| T20 | Glissement pour annuler | Fonctionnalité volontairement absente | Non implémenté (facultatif) | Non implémenté |
| T21 | Ajouter deux fois le même atelier | Aucun doublon | Tests unitaires `duplicateWorkshopIsRejectedIgnoringCase` et `exactDuplicateWorkshopIsRejected` | Réussi |
| T22 | Bouton Admin OFF puis ON | Icône, libellé, couleur et bandeau changent | Implémentation vérifiée dans `updateAdminUi`; à confirmer en clair/sombre | À faire |
| T23 | Admin ON puis appui sur le bouton | Confirmation de sortie, contrôles masqués | Implémentation vérifiée dans `onAdminButtonClicked` et `updateAdminUi`; à confirmer à l’écran | À faire |

Tests manuels encore nécessaires : T10, T11, T13, T14, T17, T18, T19, T22, T23. T15 et T16 nécessitent aussi une vérification visuelle complète de toutes les valeurs restaurées.

| N° | Action/Données | Attendu | Observé | Statut |
|---|---|---|---|---|
| T24 | Appuyer sur « Détails » sur un atelier | Dialogue avec titre, club, date, salle, instructeur, places | À vérifier manuellement sur téléphone | À faire |
| T25 | Admin ajoute un atelier avec instructeur et salle | Atelier ajouté ; Détails affiche les informations | À vérifier manuellement sur téléphone | À faire |
| T26 | Ajout sans instructeur ou sans salle | Toast d’erreur ; aucun atelier ajouté | À vérifier manuellement sur téléphone | À faire |
| T27 | Admin édite date, salle, instructeur, titre et club d’un atelier réservé | Détails actualisés, réservations conservées, chips actualisés | Test unitaire `editDetailsSuccessPreservesReservationsAndNoFalseSelfDuplicate`; contrôle visuel ADB requis | À faire |
| T28 | Ajout ou édition dans une salle déjà utilisée à cette date | « Salle déjà occupée à cette date » | Tests unitaires `addWorkshopRejectsRoomConflictAndAddsValidSession` et `editDetailsRejectsRoomConflictButAllowsNonConflictAndPastDate` | Réussi |
| T29 | Recherche par nom d’instructeur ou par salle | Atelier correspondant affiché | Tests unitaires `searchMatchesInstructorAndRoom` | Réussi |
| T30 | Exporter les ateliers | Instructeur et salle présents dans le texte | Tests unitaires `exportStartsWithAppNameAndContainsWorkshopDetails` | Réussi |
| T31 | Consulter Vue C | Chaque réservation affiche « Salle : I05 » ou la salle correspondante | À vérifier manuellement sur téléphone | À faire |

| T32 | Popups PIN, ajout, édition, suppression, annulation et détails en clair/sombre | Apparence cohérente, erreurs inline et dialogue conservé en cas d’erreur | MaterialAlertDialogBuilder est utilisé ; vérification visuelle et erreurs inline à faire | À faire |
| T33 | Transitions, insertions/suppressions, promotion et animations désactivées | Animations fluides et ignorées quand l’échelle système vaut 0 | Code compilé ; test visuel et réglage système à faire | À faire |
| T34 | Échelle de police 200 % et grande taille d’affichage | Aucun texte critique tronqué ou superposé | `uiautomator dump` exécuté à `font_scale=2.0`; validation visuelle à faire | À faire |
| T35 | Parcours TalkBack | Ordre de focus, résumés, actions et annonces accessibles | À vérifier manuellement sur téléphone | À faire |
| T36 | Paysage et écran 320dp | Écran utilisable sans chevauchement | Non exécuté sur un profil 320dp dédié | À faire |
| T37 | Monkey 3000 événements | Aucun crash ni ANR | `adb shell monkey -p tn.issatso.clubs --throttle 100 --ignore-crashes -v 3000` : 3000 événements, `Monkey finished`, PID `18999`, aucune erreur FATAL/ANR | Réussi |
| T38 | Double appui rapide sur Réserver, Confirmer, Annuler, Ajouter, Supprimer et Exporter | Une seule action métier | Non exécuté | À faire |
| T39 | Contraste AA dans les deux thèmes | Ratios conformes WCAG AA | Palette auditée par conception ; tableau de ratios complet non exécuté | À faire |
