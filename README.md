# TP 6 – Service Banque en CORBA

**Matière :** Développement d'applications réparties – LSI 3
**Établissement :** Faculté des Sciences de Sfax – Département d'informatique et des communications
**Année universitaire :** 2026 – 2027
**Réalisé par :** Fatma Mhadhbi (individuel)

---

## 🎯 Objectif

Développer une application répartie client-serveur en **Java** avec le middleware **CORBA** pour gérer des comptes bancaires. Le serveur expose les opérations suivantes :

- créer un compte avec un solde initial ;
- verser une somme sur un compte (à partir de son code) ;
- retirer une somme d'un compte (à partir de son code) ;
- consulter un compte (à partir de son code) ;
- consulter la liste de tous les comptes ;
- convertir un montant d'Euro (€) en Dinar Tunisien (DT).

## 🧰 Environnement

| Outil | Version |
|---|---|
| JDK | Eclipse Temurin 1.8.0_504 |
| IDE | IntelliJ IDEA |
| Build | Maven (`maven.compiler.source/target = 8`) |
| Terminal | PowerShell |
| Gestion de versions | Git / GitHub |

## 📁 Structure du dépôt

```
FatmaMhadhbi_LSI3_DevAppReparties/
├── TP6_CorbaBanque_Server/
│   ├── pom.xml
│   └── src/main/java/
│       ├── Banque.idl
│       ├── jndi.properties
│       ├── corbaBanque/          # code généré par idlj
│       ├── service/BanqueImpl.java
│       └── corbaServer/BanqueServer.java
├── TP6_CorbaBanque_Client/
│   ├── pom.xml
│   └── src/main/java/
│       ├── Banque.idl
│       ├── jndi.properties
│       ├── corbaBanque/          # code généré par idlj
│       └── corbaClient/BanqueClient.java
└── README.md
```

---

## Étape 1 – Préparation de l'espace de travail et de l'environnement

1. Création du répertoire de travail `FatmaMhadhbi_LSI3_DevAppReparties`, ouvert comme workspace dans l'IDE.
2. Installation du **JDK 8 (Temurin 1.8.0_504)** depuis IntelliJ (*File > Project Structure > SDKs > Download JDK*), puis association au projet avec le *Language level* 8.
3. Vérification de la présence du JDK dans `C:\Users\User\.jdks` :

![Liste des JDK installés](screenshots/03_jdks_liste.png)

4. Vérification des outils CORBA du JDK 8 :

**`idlj`** (compilateur IDL → Java, version 3.2) :

![idlj version](screenshots/05_idlj_version_chemin_complet.png)

**`tnameserv`** (service de nommage), présent dans le `bin` du JDK :

![Test-Path tnameserv](screenshots/06_test_path_tnameserv.png)

5. Configuration de `JAVA_HOME` et du `PATH` pour la session courante :

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-1.8.0_504"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

![JAVA_HOME et PATH](screenshots/07_java_home.png)

6. Vérification finale : `java -version` et `idlj -version` répondent directement.

![java -version et idlj -version](screenshots/09_java_idlj_version.png)

**Synthèse :** le JDK 8 contient bien les outils CORBA (`idlj`, `tnameserv`), qui ne sont plus fournis à partir du JDK 11. Placer le JDK 8 en tête du `PATH` permet de les utiliser depuis le terminal. Ces variables ne valent que pour le terminal ouvert ; pour les rendre permanentes, il faut les ajouter aux variables d'environnement Windows.

---

## Étape 2 – Développement du serveur

### 2.1 Création du projet `TP6_CorbaBanque_Server`

Projet Maven avec le JDK 8 (source et target à 8) :

![pom.xml](screenshots/01_pom_xml.png)

Structure du projet (`src/main/java`, `src/main/resources`, `src/test/java`) :

![Structure du projet](screenshots/02_structure_projet.png)

### 2.2 Interface IDL `Banque.idl`

```idl
module corbaBanque {
    struct Compte {
        long  code;
        float solde;
    };

    typedef sequence<Compte> tabComptes;

    interface IBanqueRemote {
        void       creerCompte(in Compte cpte);
        void       verser(in float mt, in long code);
        void       retirer(in float mt, in long code);
        Compte     getCompte(in long code);
        tabComptes getComptes();
        double     conversion(in float mt);
    };
};
```

### 2.3 Compilation IDL

Depuis `src/main/java` :

```powershell
cd .\src\main\java\
idlj -fall -v Banque.idl
```

![Compilation IDL serveur](screenshots/10_idlj_serveur.png)

Le package `corbaBanque` est généré (stubs, skeletons POA, helpers, holders, interfaces `IBanqueRemoteOperations`, etc.).

### 2.4 Implémentation du servant

La classe `service.BanqueImpl` hérite de `IBanqueRemotePOA` et implémente toutes les méthodes de `IBanqueRemoteOperations` (logique métier de la banque : création, versement, retrait, consultation, liste, conversion Euro → DT).

### 2.5 Configuration JNDI (`jndi.properties`)

```properties
java.naming.factory.initial = com.sun.jndi.cosnaming.CNCtxFactory
java.naming.provider.url = iiop://localhost:900
```

### 2.6 Classe `corbaServer.BanqueServer`

Le serveur effectue dans l'ordre :

1. initialisation de l'**ORB** ;
2. récupération du **RootPOA** et activation du **POAManager** ;
3. configuration de l'annuaire **JNDI** ;
4. création du servant `BanqueImpl` ;
5. enregistrement (`bind`) de la référence dans l'annuaire ;
6. mise de l'ORB en attente des requêtes clients (`orb.run()`).

**Synthèse :** `idlj` transforme le contrat IDL, indépendant du langage, en code Java prêt à l'emploi. Le servant contient la logique métier. Le serveur publie sa référence d'objet (IOR) dans le service de nommage pour que les clients puissent la retrouver.

---

## Étape 3 – Développement du client

1. Création du projet `TP6_CorbaBanque_Client`.
2. Copie de `Banque.idl` et `jndi.properties` dans `src/main/java`.
3. Recompilation de l'IDL côté client :

```powershell
cd .\src\main\java\
idlj -fall -v Banque.idl
```

![Compilation IDL client](screenshots/11_idlj_client.png)

4. Classe `corbaClient.BanqueClient` :
   - lecture des propriétés JNDI ;
   - recherche (`lookup`) de la référence de l'objet distant `banque` ;
   - conversion avec `IBanqueRemoteHelper.narrow(...)` pour obtenir le **stub** ;
   - appel et test de chaque méthode distante : `creerCompte`, `verser`, `retirer`, `getCompte`, `getComptes`, `conversion`.

**Synthèse :** le client n'a besoin que du code généré à partir du même fichier IDL. Il manipule l'objet distant comme un objet local grâce au stub, qui masque toute la communication réseau (IIOP).

---

## Étape 4 – Exécution de l'application

L'ordre d'initialisation doit être respecté : **annuaire → serveur → client**.

### 4.1 Lancement du service de nommage

```powershell
tnameserv -ORBInitialPort 900
```

![tnameserv](screenshots/12_tnameserv.png)

Le message **« Prêt. »** confirme que l'annuaire écoute sur le port 900.

### 4.2 Lancement du serveur (`BanqueServer`)

![Serveur démarré](screenshots/13_serveur_demarre.png)

L'objet `banque` est enregistré dans l'annuaire et le serveur attend les clients.

### 4.3 Lancement du client (`BanqueClient`)

![Résultat du client](screenshots/14_client_resultat.png)

Résultats obtenus :

| Opération | Résultat |
|---|---|
| Création de compte | `Compte créé.` |
| Versement | `Versement effectué.` |
| Retrait | `Retrait effectué.` |
| Consultation d'un compte | `Compte : 1`, `Solde : 1100.0` |
| Liste des comptes | `Code : 1 \| Solde : 1100.0` |
| Conversion | `100 Euros = 335.0 DT` |

**Synthèse :** toutes les opérations distantes fonctionnent. Le client dialogue avec le serveur via l'ORB, et le taux de conversion utilisé est de 1 € = 3,35 DT. Le programme se termine avec le code de sortie 0, sans erreur.

---

## Étape 5 – Dépôt des livrables

- Projets déposés sur GitHub.
- Lien du dépôt déposé sur l'espace de dépôt de la plateforme ENT.
- Ce fichier `README.md` contient les captures d'écran de chaque étape avec une synthèse.

## ✅ Conclusion

Ce TP a permis de mettre en œuvre une application répartie complète avec CORBA : définition du contrat en IDL, génération du code avec `idlj`, implémentation du servant, publication via le service de nommage (JNDI/`tnameserv`), puis invocation distante depuis un client Java à travers un stub.
