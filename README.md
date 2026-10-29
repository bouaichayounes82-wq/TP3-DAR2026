# TP3 : Serveur multi-threads, sockets TCP en mode connecté (Activités 3-1 et 3-2)

**Matière :** Développement d'applications réparties
**Étudiant :** Younes Bouaicha
**Projet Eclipse :** `TP3_DAR2026`

---

## 1. Objectif

Faire évoluer l'application Client/Serveur des TP1 et TP2, qui ne gérait qu'un seul client par exécution, vers un **serveur multi-thread** capable de traiter plusieurs clients simultanément avec les sockets TCP en Java.

## 2. Environnement

- JDK 21 (JavaSE-21)
- Eclipse IDE for Java Developers
- macOS
- Git / GitHub

## 3. Structure du projet

```
TP3_DAR2026/
└── src/
    ├── ServerPackage/
    │   └── ServeurMultiThread.java   # serveur : boucle accept() + un thread par client
    └── ClientPackage/
        ├── GestionClient.java        # Runnable : dialogue avec UN client
        └── ClientTP3.java            # client Java (adresse du serveur configurable)
```

## 4. Principe de fonctionnement

**Serveur (`ServeurMultiThread`)**

1. Crée un `ServerSocket` sur le port **1234**.
2. Boucle infinie : `accept()` attend un client.
3. À chaque connexion, incrémente un compteur (`compteurClient`) et affiche côté serveur le **numéro d'ordre** et l'**adresse IP + port** du client grâce à `getRemoteSocketAddress()`.
4. Crée un `Thread` avec un objet `GestionClient` et le démarre, puis retourne immédiatement à `accept()` pour accepter le client suivant.

```java
while (true) {
    Socket client = server.accept();
    compteurClient++;
    System.out.println("Client n°" + compteurClient + " connecté depuis " + client.getRemoteSocketAddress());
    new Thread(new GestionClient(client, compteurClient)).start();
}
```

**Gestion d'un client (`GestionClient`)**

Dans `run()`, le thread envoie au client son numéro d'ordre (`vous etes le client n° X`) puis ferme la socket (`try-with-resources`).

**Client Java (`ClientTP3`)**

Se connecte avec `InetAddress` et `InetSocketAddress` à l'adresse du serveur (variable `adresseServeur`), lit le message du serveur et l'affiche.

## 5. Tests

### 5.1 Tests locaux avec plusieurs clients `nc`

`telnet` n'est pas installé par défaut sur macOS. J'ai utilisé **`nc` (netcat)**, qui joue exactement le même rôle (client TCP en ligne de commande) :

```bash
nc localhost 1234
```

Plusieurs terminaux ont été ouverts : chaque client reçoit son propre numéro (1, 2, 3, ...) et le serveur reste disponible pour les suivants.

![Serveur avec plusieurs clients](screenshots/01-serveur-clients.png)

![Clients nc dans le terminal](screenshots/02-clients-nc.png)

### 5.2 Test avec le client Java (localhost)

Avec `adresseServeur = "localhost"` :

![Client Java en local](screenshots/03-client-java-localhost.png)

Côté serveur, le client est vu depuis `127.0.0.1`.

### 5.3 Test en réseau local

Pour tester avec l'adresse réseau du serveur et non avec `localhost`, l'adresse IP de la machine a été récupérée avec `ipconfig getifaddr en0` (**192.168.1.16**), puis `adresseServeur` a été changée :

```java
String adresseServeur = "192.168.1.16";
```

Le client se connecte via `InetAddress.getByName(...)` et `InetSocketAddress`. Le serveur affiche alors l'adresse réseau du client (`/192.168.1.16:49186`) au lieu de `localhost`, ce qui prouve que le serveur accepte les connexions arrivant par l'interface réseau.

![Client Java en réseau local](screenshots/04-client-java-reseau.png)

> **Remarque :** je n'avais pas de seconde machine disponible. Le test réseau a donc été fait sur la même machine, en passant par l'adresse IP du réseau Wi-Fi. Pour une machine distante, il suffit de lancer `ClientTP3` avec `adresseServeur` égale à l'IP du serveur (même réseau, pare-feu autorisant Java).

## 6. Difficultés rencontrées

| Problème | Cause | Solution |
|---|---|---|
| `java.net.BindException: Address already in use` | Une ancienne instance du serveur tournait encore (boucle `while(true)`) et occupait le port 1234 | Trouver le processus avec `lsof -nP -iTCP:1234 -sTCP:LISTEN`, l'arrêter avec `kill <PID>`, et toujours arrêter le serveur avec le carré rouge de la console avant de le relancer |
| Erreur de compilation sur `try (client; ...)` | La variable doit être `final` ou effectivement finale | Déclarer les champs `private final Socket client;` et `private final int numeroClient;` |
| Message affiché sans espaces | Concaténation sans espaces entre les chaînes | Ajout des espaces dans le `println` |

## 7. Synthèse

- Un serveur **mono-thread** traite un client à la fois : les suivants attendent. Avec un **thread par client**, la boucle principale ne fait qu'`accept()` et délègue le dialogue à `GestionClient`, ce qui permet de servir plusieurs clients en parallèle.
- Le serveur identifie chaque client par un **numéro d'ordre** et son **adresse IP:port** (`getRemoteSocketAddress()`).
- La socket de chaque client est fermée à la fin de son thread (`try-with-resources`), pour ne pas laisser de connexions ouvertes.
- Le port d'écoute est unique : une seule instance du serveur peut tourner à la fois, sinon `BindException`.

## 8. Exécution

1. Lancer `ServerPackage.ServeurMultiThread`.
2. Lancer un ou plusieurs clients : `nc localhost 1234` ou `ClientPackage.ClientTP3` (adapter `adresseServeur`).
3. Arrêter le serveur depuis la console Eclipse avant de le relancer.

---

# Activité 3-2 : Amélioration de l'application calculatrice (serveur multi-thread)

## 1. Objectif

Reprendre le service de calculatrice par objets du TP2 (activité 2-2) pour qu'un même serveur accepte **plusieurs clients simultanément**, avec **un thread dédié par client** (`ClientProcess`), et protéger par synchronisation un **compteur global** d'opérations partagé entre les threads.

## 2. Fichiers ajoutés

```
src/
├── CommonPackage/
│   └── Operation.java            # objet Serializable échangé : nombre1, opérateur, nombre2
├── ServerPackage/
│   ├── ServeurCalculatrice.java  # port 1235 : boucle accept() + un thread par client
│   └── ClientProcess.java        # Runnable : readObject -> calcul -> writeObject, compteur global
└── ClientPackage/
    ├── ClientCalculatrice.java   # client interactif (saisie au clavier, adresse du serveur configurable)
    └── TestCharge.java           # 5 clients simultanés x 100 opérations (test de charge)
```

Le service de l'activité 3-1 (`ServeurMultiThread`, port 1234) est conservé tel quel. Le serveur calculatrice utilise le **port 1235** pour que les deux serveurs puissent tourner en même temps sans conflit.

## 3. Communication par objets

- Le client envoie un objet `Operation` (classe `Serializable`) avec `ObjectOutputStream.writeObject()`.
- Le serveur lit l'objet, calcule, puis renvoie le résultat (`Double`).
- Pour éviter un blocage (deadlock) à l'ouverture des flux, **l'`ObjectOutputStream` est créé en premier et `flush()` est appelé** avant la création de l'`ObjectInputStream`, des deux côtés.
- Chaque client peut envoyer autant d'opérations qu'il veut. Quand il se ferme, le serveur reçoit une `EOFException`, traitée comme une fin normale (`Client n°X déconnecté.`).
- Division par zéro et opérateur invalide : le serveur renvoie `NaN`.

## 4. Gestion de l'accès concurrent : compteur global

Le compteur du nombre total d'opérations traitées est une **ressource critique** partagée entre tous les threads `ClientProcess`. Il est déclaré `static` (une seule copie pour tous les clients) et modifié dans une méthode `synchronized` :

```java
private static int compteurGlobal = 0;

private static synchronized int incrementerCompteur() {
    compteurGlobal++;
    return compteurGlobal;
}
```

À chaque opération calculée, le thread appelle `incrementerCompteur()` et affiche la valeur retournée dans la console du serveur. La méthode étant `synchronized`, **un seul thread à la fois** peut lire puis modifier le compteur (verrou sur la classe `ClientProcess`). L'incrément et la lecture sont dans la même section critique, donc la valeur affichée est cohérente.

## 5. Tests

### 5.1 Client interactif

Test d'un client qui envoie plusieurs opérations (`55 + 25`, `55 * 25`, `20 / 4`) :

![Client calculatrice](screenshots/05-calculatrice-client.png)

### 5.2 Plusieurs clients en parallèle

`TestCharge` lance **5 clients simultanés** (un thread par client), chacun envoyant **100 opérations**. Dans la console du serveur, les messages des différents clients sont **entremêlés**, ce qui montre qu'ils sont traités en parallèle :

![Serveur : clients entremêlés](screenshots/06-serveur-clients-meles.png)

### 5.3 Cohérence du compteur global

Après le test, la dernière ligne du serveur affiche `Compteur global : 500` (5 clients x 100 opérations) : aucune opération n'est perdue ni comptée deux fois.

![Compteur global = 500](screenshots/07-compteur-500.png)

### 5.4 Test en réseau local

Je n'avais pas de seconde machine. Comme pour l'activité 3-1, j'ai remplacé `localhost` par l'adresse IP du réseau local de la machine serveur (`192.168.1.16`) dans `ClientCalculatrice` :

```java
String adresseServeur = "192.168.1.16";
```

Le serveur affiche alors `Client n°X connecté depuis /192.168.1.16:...`. Pour une machine distante, il suffit d'indiquer l'adresse IP du serveur (même réseau, pare-feu autorisant Java).

![Test réseau local](screenshots/08-calculatrice-reseau.png)

### 5.5 Démonstration : que se passe-t-il sans `synchronized` ?

Pour vérifier l'utilité de la synchronisation, j'ai retiré `synchronized` de `incrementerCompteur()` en ajoutant une courte pause entre la lecture et l'écriture du compteur :

```java
private static int incrementerCompteur() {
    int temp = compteurGlobal;
    try { Thread.sleep(5); } catch (InterruptedException e) { }
    compteurGlobal = temp + 1;
    return compteurGlobal;
}
```

Avec les mêmes 5 clients x 100 opérations, le compteur final est **XXX** au lieu de 500 : plusieurs threads lisent la même valeur et s'écrasent mutuellement (**condition de concurrence**). Après avoir remis `synchronized`, le compteur atteint de nouveau exactement 500.

![Sans synchronized](screenshots/09-sans-synchronized.png)

## 6. Difficultés rencontrées

| Problème | Solution |
|---|---|
| Conflit de port entre les deux serveurs | Serveur calculatrice sur le port 1235, serveur 3-1 sur le port 1234 |
| Risque de deadlock avec les flux d'objets | Créer l'`ObjectOutputStream` et appeler `flush()` avant de créer l'`ObjectInputStream` |
| `EOFException` quand un client se déconnecte | Intercepter l'exception et afficher « Client n°X déconnecté. » |
| `serializable.java` (interface vide) dans le `commonPackage` du TP2 | Non reprise : `Operation` implémente `java.io.Serializable` |

## 7. Synthèse

- Le serveur de l'activité 3-2 suit le même schéma que celui de l'activité 3-1 : `accept()` dans une boucle, puis un thread dédié (`ClientProcess`) qui gère la conversation avec chaque client. Les clients sont donc indépendants les uns des autres.
- L'échange par objets (`Operation` sérialisable) remplace l'échange de texte, ce qui évite d'analyser des chaînes de caractères côté serveur.
- Le compteur global illustre le problème de l'**accès concurrent** : sans synchronisation, des mises à jour sont perdues. Avec `synchronized`, le résultat est toujours exact (500 pour le test de charge).
- Le test en réseau local est fait en passant par l'adresse IP de la machine plutôt que `localhost`.