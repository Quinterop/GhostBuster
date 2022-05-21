## Commandes principales

### Compilation 
`make`

### Nettoyage
`make clean`

### Lancement du serveur
`./server port`

### Lancement du client [APRES LE SERVEUR]
`java Client id port -Djava.net.preferIPv4Stack=true`


Nous avons implémenté le serveur en C et le client en Java. Le client dispose d'un mode verbose qui indique au joueur les actions à sa disposition, ainsi que d'un mode en interface graphique qui affiche les parties du labyrinthe qui ont déjà été explorées par le joueur.

