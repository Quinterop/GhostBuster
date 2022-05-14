#include"server.h"

uint8_t n = 0;
struct lobby parties[255];

void* joueur(void* sock2)
{
    // Déclaration des variables
    int* sock = (int*) sock2;
    struct player info_joueur;
    info_joueur.sock = *sock;
    info_joueur.etat = 0;

    int read_size;
    char message[5], buffer[3];

    // Envoi du message [GAMES_n***] au joueur
    games(info_joueur.sock);

    // Envoi des messages [OGAME_m_s***] au joueur
    ogame(info_joueur.sock);

    // Réception du message
    while(1)
    {
        printf("En attente d'une requête [NEWPL_id_port***], [REGIS_id_port_m***], [START***], [UNREG***], [SIZE?_m***], [LIST?_m***], [GAME?***].\n");
        read_size = read(info_joueur.sock, message, 5);
        if(0 >= read_size) 
        {
            perror("Le client s'est déconnecté.\n");
            return 0;
        }
        message[5] = '\0';
        printf("Requête reçue : %s\n", message);

        if(strcmp(message, "NEWPL") == 0) // [NEWPL_id_port***]
        {
            newpl_regis(&info_joueur, 0);
        }
        else if(strcmp(message, "REGIS") == 0) // [REGIS_id_port_m***]
        {
            newpl_regis(&info_joueur, 1);
        }
        else if(strcmp(message, "START") == 0) // [START***]
        {
            read(info_joueur.sock, buffer, 3); // ***
            if(info_joueur.etat == 1)
            {
                break;
            }
        }
        else if(strcmp(message, "UNREG") == 0) // [UNREG***]
        {
            unreg(&info_joueur);
        }
        else if(strcmp(message, "SIZE?") == 0) // [SIZE?_m***]
        {
            size(&info_joueur);
        }
        else if(strcmp(message, "LIST?") == 0) // [LIST?_m***]
        {
            list(&info_joueur);
        }
        else if(strcmp(message, "GAME?") == 0) // [GAME?***]
        {
            read(info_joueur.sock, buffer, 3); // ***
            games(info_joueur.sock);
            ogame(info_joueur.sock);
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [NEWPL_id_port***]/[REGIS_id_port_m***]/[START***]/[UNREG***]/[SIZE?_m***]/[LIST?_m***]/[GAME?***]).\n");
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

void newpl_regis(struct player* info_joueur, uint8_t is_regis)
{
    char id[9], port[5], buffer[3];
    uint8_t i, j;
            
    read((*info_joueur).sock, buffer, 1); // _
    read((*info_joueur).sock, id, 8); // id
    read((*info_joueur).sock, buffer, 1); // _
    read((*info_joueur).sock, port, 4); // port
    if(is_regis) // [REGIS_id_port_m***]
    {
        read((*info_joueur).sock, buffer, 1); // _
        read((*info_joueur).sock, (&(*info_joueur).m), sizeof(uint8_t)); // m
    }
    else // [NEWPL_id_port***]
    {
        for(i = 0; parties[i].etat != 0; i++){}
        (*info_joueur).m = i;
    }
    read((*info_joueur).sock, buffer, 3); // ***
    id[8] = '\0', port[4] = '\0';
    printf("id : %s\nport : %s\nm : %d\n", id, port, (*info_joueur).m);

    if((*info_joueur).etat == 1)
    {
        printf("Le joueur est déjà inscrit dans une partie.\n");
        if(write((*info_joueur).sock, "REGNO***", strlen("REGNO***")) == -1)
        {
            perror("Erreur lors de l'envoi du message [REGNO***].\n");
        }
    }
    for(uint8_t a = 0; a < strlen(id); a++)
    {
        if(!isalnum(id[a]))
        {
            printf("L'id est invalide (ne contient pas uniquement des caractères alphanumériques)'.\n");
            if(write((*info_joueur).sock, "REGNO***", strlen("REGNO***")) == -1)
            {
                perror("Erreur lors de l'envoi du message [REGNO***].\n");
            }
        }
    }
    // Création du socket UDP
    int sock_udp;
    struct sockaddr_in sockaddress;
    struct addrinfo *first_info, hints;

    memset(&hints, 0, sizeof(struct addrinfo));
    sock_udp = socket(PF_INET, SOCK_DGRAM, 0);
    if(sock_udp == -1)
    {
        printf("Erreur lors de la création du socket.\n");
        if(write((*info_joueur).sock, "REGNO***", strlen("REGNO***")) == -1)
        {
            perror("Erreur lors de l'envoi du message [REGNO***].\n");
        }
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
        if(write((*info_joueur).sock, "REGNO***", strlen("REGNO***")) == -1)
        {
            perror("Erreur lors de l'envoi du message [REGNO***].\n");
        }
    }
    for(j = 0; parties[i].disponibilite[j] != 0; j++){}
    parties[i].disponibilite[j] = 1;

    // Attribution de valeurs par défaut s'il s'agit d'une nouvelle partie
    if(parties[i].etat == 0)
    {
        n++;
        parties[i].largeur = LARGEUR_DEFAUT;
        parties[i].hauteur = HAUTEUR_DEFAUT;
        parties[i].etat = 1;
    }

    parties[i].s += 1;
    strcpy((*info_joueur).id, id);
    strcpy(parties[i].joueurs[j].id, id);
    strcpy(parties[i].joueurs[j].port, port);
    (*info_joueur).etat = 1;

    char regok[10] = "REGOK ";
    memcpy(regok + 6, (&(*info_joueur).m), sizeof(uint8_t)); // m
    memcpy(regok + 6 + sizeof(uint8_t), "***", strlen("***"));
    if(write((*info_joueur).sock, regok, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [REGOK m***].\n");
    }
    printf("Le joueur %s est inscrit dans la partie numéro %d.\n", id, (*info_joueur).m);
}

void unreg(struct player* info_joueur)
{
    char buffer[3];

    read((*info_joueur).sock, buffer, 3); // ***
    if((*info_joueur).etat != 1)
    {
        char dunno[8] = "DUNNO***";
        if(write((*info_joueur).sock, dunno, strlen(dunno)) == -1)
        {
            perror("Erreur lors de l'envoi du message [DUNNO***].\n");
        }
        return;
    }
    parties[(*info_joueur).m].disponibilite[(*info_joueur).i] = 0;
    parties[(*info_joueur).m].s--;

    if(parties[(*info_joueur).m].s == 0)
    {
        parties[(*info_joueur).m].etat = 0;
        n--;
    }

    char unrok[10] = "UNROK m***";
    memcpy(unrok + 6, (&(*info_joueur).m), sizeof(uint8_t));
    if(write((*info_joueur).sock, unrok, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [UNROK_m***].\n");
    }
}

void size(struct player* info_joueur)
{
    char buffer[3];
    
    read((*info_joueur).sock, buffer, 1); // _
    read((*info_joueur).sock, (&(*info_joueur).m), sizeof(uint8_t)); // m
    read((*info_joueur).sock, buffer, 3); // ***
    if(parties[(*info_joueur).m].etat == 0)
    {
        char dunno[8] = "DUNNO***";
        if(write((*info_joueur).sock, dunno, strlen(dunno)) == -1)
        {
            perror("Erreur lors de l'envoi du message [DUNNO***].\n");
        }
        printf("Message [DUNNO***] envoyé au joueur.\n");
        return;
    }
    char size[16] = "SIZE! m hh ww***"; // [SIZE!_m_h_w***]
    memcpy(size + strlen("SIZE! "), (&(*info_joueur).m), sizeof(uint8_t));
    memcpy(size + strlen("SIZE! ") + sizeof(uint8_t) + 1, &parties[(*info_joueur).m].hauteur, sizeof(uint16_t));
    memcpy(size + strlen("SIZE! ") + sizeof(uint8_t) + sizeof(uint16_t) + 2, &parties[(*info_joueur).m].largeur, sizeof(uint16_t));
    if(write((*info_joueur).sock, size, 16) == -1)
    {
        perror("Erreur lors de l'envoi du message [SIZE!_m_h_w***].\n");
    }
    printf("Message [SIZE!_m_h_w***] envoyé au joueur (m = %d, h = %d, w = %d).\n", (*info_joueur).m, parties[(*info_joueur).m].hauteur, parties[(*info_joueur).m].largeur);
}

void list(struct player* info_joueur)
{
    char buffer[3];

    read((*info_joueur).sock, buffer, 1); // _
    read((*info_joueur).sock, (&(*info_joueur).m), sizeof(uint8_t)); // m
    read((*info_joueur).sock, buffer, 3); // ***
    if(parties[(*info_joueur).m].etat == 0)
    {
        char dunno[8] = "DUNNO***";
        if(write((*info_joueur).sock, dunno, strlen(dunno)) == -1)
        {
            perror("Erreur lors de l'envoi du message [DUNNO***].\n");
        }
        printf("Message [DUNNO***] envoyé au joueur.\n");
        return;
    }
    char list[12] = "LIST! m s***";
    memcpy(list + strlen("LIST! "), (&(*info_joueur).m), sizeof(uint8_t));
    memcpy(list + strlen("LIST! "), &parties[(*info_joueur).m].s, sizeof(uint8_t));
    if(write((*info_joueur).sock, list, 12) == -1)
    {
        perror("Erreur lors de l'envoi du message [LIST! m s***].\n");
        return;
    }
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[(*info_joueur).m].disponibilite[i] == 1)
        {
            char playr[17] = "PLAYR idididid***";
            memcpy(playr + strlen("PLAYR "), parties[(*info_joueur).m].joueurs[i].id, 8);
            if(write((*info_joueur).sock, playr, 17) == -1)
            {
                perror("Erreur lors de l'envoi du message [PLAYR id***].\n");
                return;
            }
        }
    }
}

int is_lobby_ready(uint8_t m)
{
    int a = 0;
    for(int i = 0; i < 255; i++)
    {
        if(parties[m].joueurs[i].etat == 1)
        {
            a += 1;
        }
    }
    return a > 0 && a == parties[m].s;
}

int main(int argc, char* argv[])
{
    // Déclaration des variables
    int port, sock, sock2, size;
    struct sockaddr_in sockaddress, caller;
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
        pthread_create(&th, NULL, joueur, (void*) &sock2);
    }
    if(sock2 < 0)
    {
        perror("Connexion échouée.\n");
        return 1;
    }
    return 0;
}