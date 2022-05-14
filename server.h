#include <arpa/inet.h>
#include <ctype.h>
#include <netdb.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LARGEUR_DEFAUT 6
#define HAUTEUR_DEFAUT 6

struct player
{
    int sock; // sock du joueur
    char id[8]; // id du joueur
    char port[5]; // port UDP du joueur
    uint8_t etat; // 0 -> inscrit dans aucune partie, 1 -> inscrit dans une partie mais non lancée, 2 -> en train de jouer
    uint8_t i; // index dans les tableaux de lobby
    uint8_t m; // partie à laquelle le joueur est inscrit
};

struct lobby
{
    struct player joueurs[255];
    uint8_t disponibilite[255]; // cases disponible du tableau joueurs (0 / 1)
    uint8_t etat; // 0 -> inoccupé, 1 -> partie non lancée mais occupée, 2 -> partie en cours
    uint8_t s; // nombre de joueurs inscrits
    uint16_t largeur; // largeur du plateau
    uint16_t hauteur; // hauteur du plateau
};

void* joueur(void* sock2);
void games(int sock);
void ogame(int sock);
void newpl_regis(struct player* info_joueur, uint8_t is_regis);
void unreg(struct player* info_joueur);
void size(struct player* info_joueur);
void list(struct player* info_joueur);
int is_lobby_ready(uint8_t m);