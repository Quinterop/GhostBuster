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

struct user_info
{
    int sock_tcp;
    uint32_t sin_addr;
    struct sockaddr* saddr;
};

struct lobby
{
    uint8_t etat; // 0 -> inoccupé, 1 -> partie non lancée mais occupée, 2 -> partie en cours
    char port[4]; // numéro de port du lobby
    char ids[100][9]; // ids des joueurs inscrits dans la partie
    uint8_t disponibilite[100]; // cases disponible du tableau 2D ids (0 / 1)
    uint8_t start[100]; // joueurs étant prêt à lancer la partie (0 / 1)
    uint8_t s; // nombre de joueurs inscrits
};

void* joueur(void* info);
void* comm_udp(void* info);

uint8_t n = 0;
struct lobby parties[255];

int main(int argc, char* argv[])
{
    // Déclaration des variables
    int port, sock, sock2, size;
    struct sockaddr_in sockaddress, caller;
    struct user_info info;
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
        info.sock_tcp = sock2;
        info.sin_addr = caller.sin_addr.s_addr;
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
    struct user_info* current_user = (struct user_info*) info;
    int sock_tcp = current_user -> sock_tcp;
    int read_size, tampon = 0;
    char message[5], buffer[3];

    // Envoi du message [GAMES_n***] au joueur
    char games[6 + 1 + 3];
    memcpy(games, "GAMES ", strlen("GAMES "));
    tampon += strlen("GAMES ");
    memcpy(games + tampon, &n, sizeof(uint8_t));
    tampon += sizeof(uint8_t);
    memcpy(games + tampon, "***", strlen("***"));
    tampon += strlen("***");
    if(write(sock_tcp, games, tampon) == -1)
    {
        perror("Erreur lors de l'envoi du message \"GAMES\".\n");
        return NULL;
    }
    tampon = 0;
    printf("Message [GAMES_n***] envoyé au joueur.\n");

    // Envoi des messages [OGAME_m_s***]
    char ogame[12] = "OGAME m s***";
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[i].etat == 1)
        {
            memcpy(ogame + 6, &i, sizeof(uint8_t)); // m
            memcpy(ogame + 6 + sizeof(uint8_t) + 1, &parties[i].s, sizeof(uint8_t)); // s
            write(sock_tcp, ogame, 11);
        }
    }
    printf("Message(s) [OGAME_m_s***] envoyé(s) au joueur.\n");

    // Réception du message [NEWPL_id_port***] / [REGIS_id_port_m***]
    char id[9], port[5];
    uint8_t m;
    int sock_udp;
    struct sockaddr_in sockaddress_udp;
    struct addrinfo *first_info, hints;

    while(1)
    {
        printf("En attente d'une requête [NEWPL_id_port***] ou [REGIS_id_port_m***].\n");
        read(sock_tcp, message, 5);
        printf("Requête reçue : \"%s\"\n", message);

        // Parsing de la requête [NEWPL␣id␣port***] / [REGIS␣id␣port␣m***]
        if(strcmp(message, "NEWPL") == 0)
        {
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, id, 8); // id
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, port, 4); // port
            read(sock_tcp, buffer, 3); // ***

            uint8_t i;
            for(i = 0; parties[i].etat != 0; i++){}

            m = i;
        }
        else if(strcmp(message, "REGIS") == 0)
        {
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, id, 8); // id
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, port, 4); // port
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, &m, sizeof(uint8_t)); // m
            read(sock_tcp, buffer, 3); // ***
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [NEWPL_id_port***] ou [REGIS_id_port_m***]).\n");
            return NULL;
        }
        printf("ID : \"%s\"\nPort : \"%s\"\nm : \"%d\"\n", id, port, m);

        // Création du socket UDP
        memset(&hints, 0, sizeof(struct addrinfo));
        sock_udp = socket(PF_INET, SOCK_DGRAM, 0);
        if(sock_udp == -1)
        {
            printf("Erreur lors de la création du socket.\n");
            return NULL;
        }
        sockaddress_udp.sin_family = AF_INET;
        sockaddress_udp.sin_port = htons(atoi(port));
        sockaddress_udp.sin_addr.s_addr = htonl(INADDR_ANY);
        getaddrinfo("localhost", port, &hints, &first_info);
        if(bind(sock_udp, (struct sockaddr *) &sockaddress_udp, sizeof(struct sockaddr_in)) == 0)
        {
            printf("Socket UDP créée.\n");
            break;
        }
        else
        {
            printf("Erreur lors du binding du serveur, envoi du message [REGNO***].\n");
            if(write(sock_tcp, "REGNO***", strlen("REGNO***")) == -1)
            {
                perror("Erreur lors de l'envoi du message \"REGNO\".\n");
                return NULL;
            }
        }
    }
            
    uint8_t i;
    for(i = 0; parties[m].disponibilite[i] != 0; i++){}
    parties[m].disponibilite[i] = 1;
    parties[m].etat = 1;
    parties[m].s += 1;
    strcpy(parties[m].ids[i], id);

    char regok[10] = "REGOK ";
    memcpy(regok + 6, &m, sizeof(uint8_t)); // m
    memcpy(regok + 6 + sizeof(uint8_t), "***", strlen("***"));
    if(write(sock_tcp, regok, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message \"REGOK\".\n");
        return NULL;
    }

    printf("Le joueur %s est inscrit dans la partie \"%d\".\n", id, m);

    while(1)
    {
        printf("En attente d'une requête [START***], [UNREG***], [SIZE?_m***], [LIST?_m***] ou [GAME?***].\n");

        read_size = read(sock_tcp, message, 5); // [START***], [UNREG***], [SIZE?_m***], [LIST?_m***], [GAME?***]
        message[read_size] = '\0';
        printf("Requête reçue : \"%s\"\n", message);

        if(strcmp(message, "START") == 0) // [START***]
        {
            read(sock_tcp, buffer, 3); // ***
        }
        else if(strcmp(message, "UNREG") == 0) // [UNREG***]
        {
            read(sock_tcp, buffer, 3); // ***
        }
        else if(strcmp(message, "SIZE?") == 0) // [SIZE?_m***]
        {
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, &m, sizeof(uint8_t)); // m
            read(sock_tcp, buffer, 3); // ***
        }
        else if(strcmp(message, "LIST?") == 0) // [LIST?_m***]
        {
            read(sock_tcp, buffer, 1); // _
            read(sock_tcp, &m, sizeof(uint8_t)); // m
            read(sock_tcp, buffer, 3); // ***
        }
        else if(strcmp(message, "GAME?") == 0) // [GAME?***]
        {
            read(sock_tcp, buffer, 3); // ***
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [START***]/[UNREG***]/[SIZE?_m***]/[LIST?_m***]/[GAME?***]).\n");
            return NULL;
        }
    }

    /* while(1)
    {
        // Lecture d'un message TCP
        printf("Je cherche à lire un message en TCP.\n");
        read_size = read(sock_tcp, message, 99 * sizeof(char));
        message[read_size] = '\0';

        // Vérification du message
        if(read_size == 0)
        {
            printf("Client déconnecté.\n");
            return 0;
        }
        else if(read_size == -1)
        {
            perror("La réception du message a échoué.\n");
            return NULL;
        }

        // Témoin
        printf("Message reçu en TCP : '%s'\n", message);
    
        // Réponse en TCP
        memcpy(reponse, message, strlen(message));
        memcpy(reponse + strlen(message) - 1, "feur", strlen("feur"));
        reponse[16] = '\0';
        if(write(sock_tcp, reponse, strlen(reponse)) == -1)
        {
            perror("Erreur lors du hello au pseudo reçu.\n");
            return NULL;
        }

        // Témoin
        printf("Réponse envoyée en TCP : '%s'\n", reponse);
    } */
    return 0;
}

/* void* comm_udp(void* info)
{
    // Déclaration des variables
    struct user_info* current_user = (struct user_info*) info;
    int sock_udp = current_user -> sock_udp;
    int read_size;
    struct sockaddr *saddr = current_user -> saddr;
     
    struct sockaddr_in emet;
    socklen_t a = sizeof(emet);

    while(1)
    {
        char message[16], reponse[16];

        // Lecture d'un message UDP
        printf("Je cherche à lire un message en UDP.\n");
        read_size = recvfrom(sock_udp, message, 99 * sizeof(char), 0, (struct sockaddr *) &emet, &a);
        message[read_size] = '\0';

        // Vérification du message
        if(read_size == 0)
        {
            printf("Client déconnecté.\n");
            return 0;
        }
        else if(read_size == -1)
        {
            perror("La réception du message a échoué.\n");
            return NULL;
        }

        // Témoin
        printf("Message reçu en UDP : '%s'\n", message);
    
        // Réponse en UDP
        memcpy(reponse, message, strlen(message));
        memcpy(reponse + strlen(message) - 1, "feur", strlen("feur"));
        reponse[16] = '\0';
        if(sendto(sock_udp, reponse, strlen(reponse), 0, saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
        {
            perror("Erreur lors du hello au pseudo reçu.\n");
            return NULL;
        }

        // Témoin
        printf("Réponse envoyée en UDP : '%s'\n", reponse);
    }
    return 0;
} */