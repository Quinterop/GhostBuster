#include <arpa/inet.h>
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

struct player
{
    int sock;
    char id[8];
    uint8_t inscrit; // 1 -> inscrit à une partie, 0 -> sinon
};

struct lobby
{
    uint8_t etat; // 0 -> inoccupé, 1 -> partie non lancée mais occupée, 2 -> partie en cours
    char port[255][4]; // numéro de port du lobby
    char ids[255][9]; // ids des joueurs inscrits dans la partie
    uint8_t disponibilite[255]; // cases disponible du tableau 2D ids (0 / 1)
    uint8_t start[255]; // joueurs étant prêt à lancer la partie (0 / 1)
    uint8_t s; // nombre de joueurs inscrits
    uint16_t largeur;
    uint16_t hauteur;
};

void* joueur(void* info);
void games(int sock);
void ogame(int sock);
int is_id_in_lobby(char* id, int m);
int is_lobby_ready(int m);

uint8_t n = 0;
struct lobby parties[255];

int main(int argc, char* argv[])
{
    // Déclaration des variables
    int port, sock, sock2, size;
    struct sockaddr_in sockaddress, caller;
    struct player info;
    pthread_t th;

    // Parsing de la ligne de commandes
    if(2 != argc)
    {
        printf("UTILISATION :\n./serveur port_tcp\n");
        return 0;
    }
    port = atoi(argv[1]);
    printf("Ligne de commande parsée.\n");

    // Création du socket TCP
    sock = socket(PF_INET, SOCK_STREAM, 0);
    if(sock == -1)
    {
        printf("Erreur lors de la création du socket.\n");
        return 1;
    }
    sockaddress.sin_family = AF_INET;
    sockaddress.sin_port = htons(port);
    sockaddress.sin_addr.s_addr = htonl(INADDR_ANY);
    printf("Socket TCP créée.\n");

    // Binding
    if(bind(sock, (struct sockaddr *) &sockaddress, sizeof(struct sockaddr_in)) < 0)
    {
        perror("Erreur lors du binding du serveur.\n");
        return 1;
    }
    printf("Binding fait.\n");

    // Listen
    if(listen(sock, 3) != 0)
    {
        perror("Erreur lors de la création du serveur.\n");
        return 1;
    }
    printf("En attente d'une connexion TCP...\n");
    
    size = sizeof(struct sockaddr_in);
    while(1)
    {
        sock2 = accept(sock, (struct sockaddr*) &caller, (socklen_t*) &size);
        printf("Connexion TCP acceptée.\n");
        info.sock = sock2;
        pthread_create(&th, NULL, joueur, (void*) &info);
    }
    if(sock2 < 0)
    {
        perror("Connexion échouée.\n");
        return 1;
    }
    return 0;
}

void* joueur(void* info)
{
    // Déclaration des variables
    struct player* info_joueur = (struct player*) info;
    int sock = info_joueur -> sock;
    char message[5], buffer[3];

    // Envoi du message [GAMES_n***] au joueur
    games(sock);

    // Envoi des messages [OGAME_m_s***] au joueur
    ogame(sock);

    // Réception du message
    uint8_t m, i;

    while(1)
    {
        printf("En attente d'une requête [NEWPL_id_port***], [REGIS_id_port_m***], [START***], [UNREG***], [SIZE?_m***], [LIST?_m***], [GAME?***].\n");
        read(sock, message, 5);
        message[5] = '\0';
        printf("Requête reçue : \"%s\"\n", message);

        if(strcmp(message, "NEWPL") == 0 || strcmp(message, "REGIS") == 0) // [NEWPL_id_port***] / [REGIS_id_port_m***]
        {
            char id[9], port[5];
            
            read(sock, buffer, 1); // _
            read(sock, id, 8); // id
            read(sock, buffer, 1); // _
            read(sock, port, 4); // port
            if(strcmp(message, "REGIS") == 0) // [REGIS_id_port_m***]
            {
                read(sock, buffer, 1); // _
                read(sock, &m, sizeof(uint8_t)); // m
            }
            else // [NEWPL_id_port***]
            {
                for(i = 0; parties[i].etat != 0; i++){}
                m = i;
            }
            read(sock, buffer, 3); // ***
            id[9] = '\0', port[5] = '\0';
            printf("ID : \"%s\"\nPort : \"%s\"\nm : \"%d\"\n", id, port, m);

            // Création du socket UDP
            int sock_udp;
            struct sockaddr_in sockaddress;
            struct addrinfo *first_info, hints;

            memset(&hints, 0, sizeof(struct addrinfo));
            sock_udp = socket(PF_INET, SOCK_DGRAM, 0);
            if(sock_udp == -1)
            {
                printf("Erreur lors de la création du socket.\n");
                return NULL;
            }
            sockaddress.sin_family = AF_INET;
            sockaddress.sin_port = htons(atoi(port));
            sockaddress.sin_addr.s_addr = htonl(INADDR_ANY);
            getaddrinfo("localhost", port, &hints, &first_info);
            if(bind(sock_udp, (struct sockaddr *) &sockaddress, sizeof(struct sockaddr_in)) == 0)
            {
                printf("Socket UDP créée.\n");
            }
            else
            {
                printf("Erreur lors du binding du serveur, envoi du message [REGNO***].\n");
                if(write(sock, "REGNO***", strlen("REGNO***")) == -1)
                {
                    perror("Erreur lors de l'envoi du message \"REGNO\".\n");
                    return NULL;
                }
            }
            for(i = 0; parties[m].disponibilite[i] != 0; i++){}
            parties[m].disponibilite[i] = 1;
            parties[m].etat = 1;
            parties[m].s += 1;
            strcpy(parties[m].ids[i], id);
            strcpy(parties[m].port[i], port);
            (*info_joueur).inscrit = 1;

            char regok[10] = "REGOK ";
            memcpy(regok + 6, &m, sizeof(uint8_t)); // m
            memcpy(regok + 6 + sizeof(uint8_t), "***", strlen("***"));
            if(write(sock, regok, 10) == -1)
            {
                perror("Erreur lors de l'envoi du message \"REGOK\".\n");
                return NULL;
            }

            printf("Le joueur %s est inscrit dans la partie numéro %d.\n", id, m);
        }
        else if(strcmp(message, "START") == 0) // [START***]
        {
            read(sock, buffer, 3); // ***
            if((*info_joueur).inscrit == 1)
            {
                parties[m].start[i] = 1;
                break;
            }
        }
        else if(strcmp(message, "UNREG") == 0) // [UNREG***]
        {
            read(sock, buffer, 3); // ***
            if((*info_joueur).inscrit == 1)
            {
                strcpy(parties[m].ids[i], "00000000");
                parties[m].disponibilite[i] = 0;
                parties[m].s -= 1;
                if(parties[m].s == 0)
                {
                    parties[m].etat = 0;
                }

                char unrok[10] = "UNROK m***";
                memcpy(unrok + 6, &m, sizeof(uint8_t));
                if(write(sock, unrok, 10) == -1)
                {
                    perror("Erreur lors de l'envoi du message [UNROK_m***].\n");
                }
            }
            else
            {
                char dunno[8] = "DUNNO***";
                if(write(sock, dunno, strlen(dunno)) == -1)
                {
                    perror("Erreur lors de l'envoi du message [DUNNO***].\n");
                }
            }
        }
        else if(strcmp(message, "SIZE?") == 0) // [SIZE?_m***]
        {
            read(sock, buffer, 1); // _
            read(sock, &m, sizeof(uint8_t)); // m
            read(sock, buffer, 3); // ***
            if(parties[m].etat == 0)
            {
                char dunno[8] = "DUNNO***";
                if(write(sock, dunno, strlen(dunno)) == -1)
                {
                    perror("Erreur lors de l'envoi du message [DUNNO***].\n");
                }

                printf("Message [DUNNO***] envoyé au joueur.\n");
            }
            else
            {
                char size[16] = "SIZE! m hh ww***"; // [SIZE!_m_h_w***]
                memcpy(size + strlen("SIZE! "), &m, sizeof(uint8_t));
                memcpy(size + strlen("SIZE! ") + sizeof(uint8_t) + 1, &parties[m].hauteur, sizeof(uint16_t));
                memcpy(size + strlen("SIZE! ") + sizeof(uint8_t) + sizeof(uint16_t) + 2, &parties[m].largeur, sizeof(uint16_t));
                if(write(sock, size, 16) == -1)
                {
                    perror("Erreur lors de l'envoi du message [SIZE!_m_h_w***].\n");
                }
                printf("Message [SIZE!_m_h_w***] envoyé au joueur (m = %d, h = %d, w = %d).\n", m, parties[m].hauteur, parties[m].largeur);
            }
        }
        else if(strcmp(message, "LIST?") == 0) // [LIST?_m***]
        {
            read(sock, buffer, 1); // _
            read(sock, &m, sizeof(uint8_t)); // m
            read(sock, buffer, 3); // ***
            if(parties[m].etat == 0)
            {
                char dunno[8] = "DUNNO***";
                if(write(sock, dunno, strlen(dunno)) == -1)
                {
                    perror("Erreur lors de l'envoi du message [DUNNO***].\n");
                }

                printf("Message [DUNNO***] envoyé au joueur.\n");
            }
            else
            {
                char list[12] = "LIST! m s***";
                memcpy(list + strlen("LIST! "), &m, sizeof(uint8_t));
                memcpy(list + strlen("LIST! "), &parties[m].s, sizeof(uint8_t));
                if(write(sock, list, 12) == -1)
                {
                    perror("Erreur lors de l'envoi du message [LIST! m s***].\n");
                }
                for(uint8_t i = 0; i < 255; i++)
                {
                    if(parties[m].disponibilite[i] == 1)
                    {
                        char playr[17] = "PLAYR idididid***";
                        memcpy(playr + strlen("PLAYR "), parties[m].ids[i], 8);
                        if(write(sock, playr, 17) == -1)
                        {
                            perror("Erreur lors de l'envoi du message [PLAYR id***].\n");
                        }
                    }
                }
            }
        }
        else if(strcmp(message, "GAME?") == 0) // [GAME?***]
        {
            read(sock, buffer, 3); // ***
            games(sock);
            ogame(sock);
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [NEWPL_id_port***]/[REGIS_id_port_m***]/[START***]/[UNREG***]/[SIZE?_m***]/[LIST?_m***]/[GAME?***]).\n");
            return NULL;
        }
    }
            
    return 0;
}

void games(int sock)
{
    int tampon = 0;
    char games[6 + 1 + 3];

    memcpy(games, "GAMES ", strlen("GAMES "));
    tampon += strlen("GAMES ");
    memcpy(games + tampon, &n, sizeof(uint8_t));
    tampon += sizeof(uint8_t);
    memcpy(games + tampon, "***", strlen("***"));
    tampon += strlen("***");
    if(write(sock, games, tampon) == -1)
    {
        perror("Erreur lors de l'envoi du message [GAMES_n***].\n");
    }
    printf("Message [GAMES_n***] envoyé au joueur (n = %d).\n", n);
}

void ogame(int sock)
{
    char ogame[12] = "OGAME m s***";

    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[i].etat == 1)
        {
            memcpy(ogame + 6, &i, sizeof(uint8_t)); // m
            memcpy(ogame + 6 + sizeof(uint8_t) + 1, &parties[i].s, sizeof(uint8_t)); // s
            if(write(sock, ogame, 12) == -1)
            {
                perror("Erreur lors de l'envoi du message [OGAME_m_s***].\n");
            }
        }
    }
    printf("Message(s) [OGAME_m_s***] envoyé(s) au joueur.\n");
}

int is_id_in_lobby(char* id, int m)
{
    for(int i = 0; i < 255; i++)
    {
        if(strcmp(id, parties[m].ids[i]) == 0)
        {
            return 1;
        }
    }
    return 0;
}

int is_lobby_ready(int m)
{
    int a = 0;
    for(int i = 0; i < 255; i++)
    {
        if(parties[m].start[i] == 1)
        {
            a += 1;
        }
    }
    return a == parties[m].s;
}