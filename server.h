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
#include <time.h>
#include <unistd.h>

#define LARGEUR_DEFAUT 6
#define HAUTEUR_DEFAUT 6
#define FANTOMES_DEFAUT 6

#define ESPACE_VIDE 1
#define MUR 0
#define JOUEUR 2
#define FANTOME 3

typedef struct Player
{
    char id[9]; // id du joueur
    char port[5]; // port UDP du joueur
    char p[4]; // nombre de points du joueur
    char x[4]; // coordonnée x où se trouve le joueur dans le labyrinthe
    char y[4]; // coordonnée y où se trouve le joueur dans le labyrinthe
    int sock_tcp; // sock du joueur
    int sock_udp; // sock udp du joueur
    struct sockaddr* saddr;
    uint8_t etat; // 0 -> inscrit dans aucune partie, 1 -> inscrit dans une partie mais non lancée, 2 -> en attente du lancement de partie 3 -> en train de jouer
    uint8_t i; // index dans les tableaux de lobby
    uint8_t m; // partie à laquelle le joueur est inscrit
} Player;

struct lobby
{
    char* ip; // adresse IP de multi-diffusion
    char* port; // port de multi-diffusion
    int sock; // socket de multi-diffusion
    int** plateau; // plateau de jeu
    Player* joueurs[255]; // tableau des joueurs
    struct sockaddr* saddr;
    uint8_t etat; // 0 -> inoccupé, 1 -> partie non lancée mais occupée, 2 -> partie en cours
    uint8_t f; // nombre de fantomes dans la partie
    uint8_t s; // nombre de joueurs inscrits
    uint16_t l; // largeur du plateau
    uint16_t h; // hauteur du plateau

};

void* hub(void* sock2);
void avant_partie_aux(Player* info_joueur);
void games(Player* info_joueur);
void newpl_regis(Player* info_joueur, uint8_t is_regis);
void unreg(Player* info_joueur);
void size(Player* info_joueur);
void list(Player* info_joueur);
void welco(Player* info_joueur);
void regno(Player* info_joueur);
void dunno(Player* info_joueur);

void partie_en_cours(Player* info_joueur);
void move(char d, Player* info_joueur);
void gobye(Player* info_joueur);
void glis(Player* info_joueur);
void mall(Player* info_joueur);
void send_mess(Player* info_joueur);
void endga(Player* info_joueur);

void deplacer_fantomes_aleatoirement(Player* info_joueur);
int is_lobby_ready(uint8_t m);
Player* get_first_player(Player* info_joueur);
Player* get_winner(Player* info_joueur);
void reset_game(uint8_t m);
void uint16_to_len_str(char* dest, uint16_t nombre, uint8_t n);
int** parse_txt(char* filename);