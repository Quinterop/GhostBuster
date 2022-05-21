#include "server.h"

pthread_mutex_t verrou = PTHREAD_MUTEX_INITIALIZER;
uint8_t n = 0;
struct lobby parties[255];

void* hub(void* sock2)
{
    // Déclaration des variables
    Player* info_joueur = malloc(sizeof(Player));
    info_joueur -> sock_tcp = *((int*) sock2);
    info_joueur -> etat = 0;

    avant_partie_aux(info_joueur);
    return 0;
}

void avant_partie_aux(Player* info_joueur){

    // Déclaration des variables
    int read_size;
    char buffer[3], message[6];

    // Envoi du message [GAMES_n***] et [OGAME_m_s***]
    games(info_joueur);

    // Réception du message
    while(1)
    {
        printf("\nEn attente d'une requête [NEWPL_id_port***], [REGIS_id_port_m***], [START***], [UNREG***], [SIZE?_m***], [LIST?_m***], [GAME?***].\n");
        read_size = read(info_joueur -> sock_tcp, message, 5);
        if(0 >= read_size) 
        {
            perror("Le client s'est déconnecté.\n");
            return;
        }
        message[read_size + 1] = '\0';
        printf("Requête reçue : %s\n", message);

        if(strcmp(message, "NEWPL") == 0) // [NEWPL_id_port***]
        {
            newpl_regis(info_joueur, 0);
        }
        else if(strcmp(message, "REGIS") == 0) // [REGIS_id_port_m***]
        {
            newpl_regis(info_joueur, 1);
        }
        else if(strcmp(message, "START") == 0) // [START***]
        {
            read(info_joueur -> sock_tcp, buffer, 3); // ***
            if(info_joueur -> etat == 1)
            {
                info_joueur -> etat = 2;
                break;
            }
            dunno(info_joueur);
        }
        else if(strcmp(message, "UNREG") == 0) // [UNREG***]
        {
            read(info_joueur -> sock_tcp, buffer, 3); // ***
            unreg(info_joueur);
        }
        else if(strcmp(message, "SIZE?") == 0) // [SIZE?_m***]
        {
            size(info_joueur);
        }
        else if(strcmp(message, "LIST?") == 0) // [LIST?_m***]
        {
            list(info_joueur);
        }
        else if(strcmp(message, "GAME?") == 0) // [GAME?***]
        {
            read(info_joueur -> sock_tcp, buffer, 3); // ***
            games(info_joueur);
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [NEWPL_id_port***]/[REGIS_id_port_m***]/[START***]/[UNREG***]/[SIZE?_m***]/[LIST?_m***]/[GAME?***]).\n");
        }
    }

    while(!is_lobby_ready(info_joueur -> m))
    {
        printf("Test du lobby...\n");
        sleep(5);
    }
    printf("Le lobby est prêt, la partie va commencer.\n");
    
    welco(info_joueur);

    partie_en_cours(info_joueur);
    return;
}

void games(Player* info_joueur)
{
    // Envoi du message [GAMES_n***]
    char games[10] = "GAMES n***";
    memcpy(games + 6, &n, sizeof(uint8_t));
    if(write(info_joueur -> sock_tcp, games, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [GAMES_n***].\n");
        return;
    }
    printf("Message [GAMES_n***] envoyé au joueur (n = %d).\n", n);

    // Envoi du/des message(s) [OGAME_m_s***]
    char ogame[12] = "OGAME m s***";
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[i].etat == 1)
        {
            memcpy(ogame + 6, &i, sizeof(uint8_t)); // m
            memcpy(ogame + 6 + sizeof(uint8_t) + 1, &parties[i].s, sizeof(uint8_t)); // s
            if(write(info_joueur -> sock_tcp, ogame, 12) == -1)
            {
                perror("Erreur lors de l'envoi du message [OGAME_m_s***].\n");
            }
            printf("Message [OGAME_m_s***] envoyé au joueur (m = %d, s = %d).\n", i, parties[i].s);
        }
    }
}

void newpl_regis(Player* info_joueur, uint8_t is_regis)
{
    char buffer[3], id[9], port[5];
    uint8_t i, j, m; // index de la partie et du joueur
    
    // Réception des requêtes [NEWPL_id_port***] et [REGIS_id_port_m***]
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read(info_joueur -> sock_tcp, id, 8); // id
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read(info_joueur -> sock_tcp, port, 4); // port
    id[8] = '\0', port[5] = '\0';
    if(is_regis) // Réception spécifique à [REGIS_id_port_m***]
    {
        read(info_joueur -> sock_tcp, buffer, 1); // _
        read(info_joueur -> sock_tcp, &m, sizeof(uint8_t)); // m
        read(info_joueur -> sock_tcp, buffer, 3);  // ***
        // Vérification de l'existence de la partie cherchée à être rejoindre 
        if(parties[m].etat != 1)
        {
            // Envoi du message [REGNO***] au joueur
            printf("La partie que le joueur souhaite entrer n'existe pas.\n");
            regno(info_joueur);
            return;
        }
    }
    else // Réception spécifique à [NEWPL_id_port***]
    {
        read(info_joueur -> sock_tcp, buffer, 3); // ***

        for(i = 0; parties[i].etat != 0; i++){}
        m = i;
    }

    printf("id : %s\nport : %s\nm : %u\n", id, port, m);

    // Vérification que le joueur peut s'inscrire à une partie
    if(info_joueur -> etat == 1)
    {
        printf("Le joueur est déjà inscrit dans une partie.\n");
        regno(info_joueur);
        return;
    }

    // Vérification de la légalité de l'ID du joueur
    for(uint8_t a = 0; a < strlen(id); a++)
    {
        if(!isalnum(id[a]))
        {
            // Envoi du message [REGNO***]
            printf("L'id est invalide (ne contient pas uniquement des caractères alphanumériques).\n");
            regno(info_joueur);
            return;
        }
    }

    // Création du socket UDP du joueur
    char ip[16], port_multicast[5];
    int sock_udp;
    struct addrinfo *first_info, hints, *first_info_multicast, hints_multicast;;
    struct sockaddr* saddr;

    memset(&hints, 0, sizeof(struct addrinfo));
    sock_udp = socket(PF_INET, SOCK_DGRAM, 0);
    if(sock_udp == -1)
    {
        // Envoi du message [REGNO***]
        printf("Erreur lors de la création de la socket UDP du joueur.\n");
        regno(info_joueur);
        return;
    }
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_DGRAM;
    
    if(getaddrinfo("localhost", port, &hints, &first_info) != 0 || first_info == NULL)
    {
        // Envoi du message [REGNO***]
        printf("Erreur lors du bind de la socket UDP du joueur.\n");
        regno(info_joueur);
        return;
    }
    sock_udp = sock_udp;
    saddr = first_info -> ai_addr;
    printf("Socket UDP créée.\n");
    
    // Attribution du joueur dans la liste de joueurs de la partie
    for(j = 0; parties[m].joueurs[j] != NULL; j++) {}
    parties[m].joueurs[j] = info_joueur;

    // Attribution des valeurs par défaut s'il s'agit d'une nouvelle partie
    if(parties[m].etat == 0)
    {
        strcpy(ip, "###############");
        memset(&hints_multicast, 0, sizeof(struct addrinfo));
        parties[i].sock = socket(PF_INET, SOCK_DGRAM, 0);
        hints_multicast.ai_family = AF_INET;
        hints_multicast.ai_socktype = SOCK_DGRAM;
        do
        {
            sprintf(ip, "224.4.5.6");
            sprintf(port_multicast, "%d", m + 6000);
            printf("Test de la disponibilité de l'adresse IP multicast \"%s\" avec le port %s...\n", ip, port_multicast);
        } while(getaddrinfo(ip, port_multicast, &hints_multicast, &first_info_multicast) != 0);
        printf("Test réussi.\n");
        
        pthread_mutex_lock(&verrou);
        n++;
        strcpy(parties[m].ip, ip);
        strcpy(parties[m].port, port_multicast);
        parties[m].saddr = first_info_multicast -> ai_addr;
        parties[m].etat = 1;
        pthread_mutex_unlock(&verrou);
    }
    
    // Attribution des valeurs aux informations du joueur
    info_joueur -> etat = 1;
    strcpy(info_joueur -> id, id);
    strcpy(info_joueur -> port, port);
    info_joueur -> m = m;
    info_joueur -> sock_udp = sock_udp;
    info_joueur -> saddr = saddr;

    pthread_mutex_lock(&verrou);
    parties[info_joueur -> m].s += 1;
    pthread_mutex_unlock(&verrou);

    printf("Etat de la partie %u : %u\nNombre de joueurs dans la partie %u : %u.\n", info_joueur -> m, parties[i].etat, info_joueur -> m, parties[info_joueur -> m].s);

    // Envoi du message [REGOK_m***]
    char regok[10] = "REGOK m***";
    memcpy(regok + 6, &info_joueur -> m, sizeof(uint8_t)); // m
    if(write(info_joueur -> sock_tcp, regok, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [REGOK m***].\n");
        return;
    }
    printf("Le joueur %s est inscrit dans la partie numéro %d.\n", info_joueur -> id, info_joueur -> m);
}

void unreg(Player* info_joueur)
{
    // Vérification préliminaire
    if(info_joueur -> etat != 1)
    {
        // Envoi du message [DUNNO***]
        dunno(info_joueur);
        return;
    }

    // Désinscription du joueur
    parties[info_joueur -> m].joueurs[info_joueur -> i] = NULL;
    parties[info_joueur -> m].s--;
    info_joueur -> etat = 0;

    // Supression de la partie s'il n'y a plus de joueur dans la partie
    if(parties[info_joueur -> m].s == 0)
    {
        printf("La partie %u est supprimée.\n", info_joueur -> m);
        parties[info_joueur -> m].etat = 0; 
        reset_game(info_joueur -> m);
        n--;
    }

    // Envoi du message [UNROK_m***]
    char unrok[10] = "UNROK m***";
    memcpy(unrok + 6, &info_joueur -> m, sizeof(uint8_t));
    if(write(info_joueur -> sock_tcp, unrok, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [UNROK_m***].\n");
    }
    printf("Message [UNROK_m***] envoyé au joueur (m = %u).\n", info_joueur -> m);
    return;
}

void size(Player* info_joueur)
{
    char buffer[3];
    uint8_t m;

    // Réception de la requête [SIZE?_m***]
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read(info_joueur -> sock_tcp, &m, sizeof(uint8_t)); // m
    read(info_joueur -> sock_tcp, buffer, 3); // ***

    // Vérification de l'existence de la partie demandée
    if(parties[m].etat == 0)
    {
        // Envoi du message [DUNNO***]
        dunno(info_joueur);
        return;
    }

    // Envoi du message [SIZE!_m_h_w***]
    char size[16] = "SIZE! m h. w.***";
    memcpy(size + 6, &m, sizeof(uint8_t));
    memcpy(size + 8, &parties[m].h, sizeof(uint16_t));
    memcpy(size + 11, &parties[m].l, sizeof(uint16_t));
    if(write(info_joueur -> sock_tcp, size, 16) == -1)
    {
        perror("Erreur lors de l'envoi du message [SIZE!_m_h_w***].\n");
        return;
    }
    printf("Message [SIZE!_m_h_w***] envoyé au joueur (m = %d, h = %d, w = %d).\n", m, parties[info_joueur -> m].h, parties[info_joueur -> m].l);
}

void list(Player* info_joueur)
{
    char buffer[3];
    uint8_t m;

    // Réception de la requête [LIST?_m***]
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read(info_joueur -> sock_tcp, &m, sizeof(uint8_t)); // m
    read(info_joueur -> sock_tcp, buffer, 3); // ***

    // Vérification de l'existence de la partie demandée
    if(parties[m].etat == 0)
    {
        // Envoi du message [DUNNO***]
        dunno(info_joueur);
        return;
    }

    // Envoi du message [LIST!_m_s***] 
    char list[12] = "LIST! m s***";
    memcpy(list + 6, &m, sizeof(uint8_t));
    memcpy(list + 8, &parties[m].s, sizeof(uint8_t));
    if(write(info_joueur -> sock_tcp, list, 12) == -1)
    {
        perror("Erreur lors de l'envoi du message [LIST!_m_s***].\n");
        return;
    }
    printf("Message [LIST!_m_s***] envoyé au joueur (m = %u, s = %u).\n", m, parties[m].s);

    // Envoi du/des message(s) [PLAYR_id***]
    char playr[17] = "PLAYR id......***";
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[m].joueurs[i] != NULL)
        {
            memcpy(playr + 6, parties[m].joueurs[i] -> id, 8);
            if(write(info_joueur -> sock_tcp, playr, 17) == -1)
            {
                perror("Erreur lors de l'envoi du message [PLAYR_id***].\n");
                return;
            }
            printf("Message [PLAYR_id***] envoyé au joueur (id = %s).\n", parties[m].joueurs[i] -> id);
        }
    }
}

void welco(Player* info_joueur)
{
    // Envoi du message [WELCO_m_h_w_f_ip_port***]
    char welco[39] = "WELCO m h. w. f ip############# port***";
    memcpy(welco + 6, &info_joueur -> m, sizeof(uint8_t));
    memcpy(welco + 8, &parties[info_joueur -> m].h, sizeof(uint16_t));
    memcpy(welco + 11, &parties[info_joueur -> m].l, sizeof(uint16_t));
    memcpy(welco + 14, &parties[info_joueur -> m].f, sizeof(uint8_t));
    memcpy(welco + 16, parties[info_joueur -> m].ip, strlen(parties[info_joueur -> m].ip));
    memcpy(welco + 32, parties[info_joueur -> m].port, strlen(parties[info_joueur -> m].port));
    if(write(info_joueur -> sock_tcp, welco, 39) == -1)
    {
        perror("Erreur lors de l'envoi du message [WELCO_m_h_w_f_ip_port***] au joueur.\n");
        return;
    }
    printf("Message [WELCO_m_h_w_f_ip_port***] envoyé au joueur (m = %u, h = %u, w = %u, f = %u, ip = %s, port = %s).\n", info_joueur -> m, parties[info_joueur -> m].h, parties[info_joueur -> m].l, parties[info_joueur -> m].f, parties[info_joueur -> m].ip, parties[info_joueur -> m].port);

    if(info_joueur == get_first_player(info_joueur))
    {
        parties[info_joueur -> m].plateau = parse_txt("labyrinthe0.txt");
    }

    // Définition de la position x et y de départ du joueur dans le labyrinthe
    char x_string[4], y_string[4];
    uint16_t x, y;
    do
    {  
        x = rand() % parties[info_joueur -> m].l;
        y = rand() % parties[info_joueur -> m].h;
    } while(parties[info_joueur -> m].plateau[x][y] != ESPACE_VIDE);
    uint16_to_len_str(x_string, rand() % parties[info_joueur -> m].l, 3);
    uint16_to_len_str(y_string, rand() % parties[info_joueur -> m].h, 3);
    strcpy(info_joueur -> x, x_string);
    strcpy(info_joueur -> y, y_string);

    // Envoi du message [POSIT_id_x_y***] 
    char posit[25] = "POSIT id...... x.. y..***"; 
    memcpy(posit + 6, info_joueur -> id, strlen(info_joueur -> id));
    memcpy(posit + 15, x_string, strlen(x_string));
    memcpy(posit + 19, y_string, strlen(y_string));
    if(write(info_joueur -> sock_tcp, posit, 25) == -1)
    {
        perror("Erreur lors de l'envoi du message [POSIT_id_x_y***] au joueur.\n");
        return;
    }
    printf("Message [POSIT_id_x_y***] envoyé au joueur (id = %s, x = %s, y = %s).\n", info_joueur -> id, x_string, y_string);
}

void regno(Player* info_joueur) 
{
    // Envoi du message [REGNO***]
    char regno[8] = "REGNO***";
    if(write(info_joueur -> sock_tcp, regno, 8) == -1)
    {
        perror("Erreur lors de l'envoi du message [REGNO***].\n");
        return;
    }
    printf("Message [REGNO***] envoyé au joueur.\n");
}

void dunno(Player* info_joueur)
{
    // Envoi du message [DUNNO***]
    char dunno[8] = "DUNNO***";
    if(write(info_joueur -> sock_tcp, dunno, 8) == -1)
    {
        perror("Erreur lors de l'envoi du message [DUNNO***].\n");
        return;
    }
    printf("Message [DUNNO***] envoyé au joueur.\n");
}

void partie_en_cours(Player* info_joueur)
{
    parties[info_joueur -> m].etat = 2;
    strcpy(info_joueur -> p, "0000");

    deplacer_fantomes_aleatoirement(info_joueur);

    char buffer[3], message[6];
    int read_size;
    
    // Réception du message
    while(1)
    {
        printf("\nEn attente d'une requête [UPMOV_d***], [DOMOV_d***], [LEMOV_d***], [RIMOV_d***], [IQUIT***], [GLIS?***], [MALL?_mess***], [SEND?_id_mess***].\n");
        read_size = read(info_joueur -> sock_tcp, message, 5);
        if(read_size<=0) 
        {
            perror("Le client s'est déconnecté.\n");
            return;
        }
        message[read_size + 1] = '\0';
        printf("Requête reçue : %s\n", message);

        if(strcmp(message, "UPMOV") == 0) // [UPMOV_d***]
        {
            move('U', info_joueur);
        }
        else if(strcmp(message, "DOMOV") == 0) // [DOMOV_d***]
        {
            move('D', info_joueur);
        }
        else if(strcmp(message, "LEMOV") == 0) // [LEMOV_d***]
        {
            move('L', info_joueur);
        }
        else if(strcmp(message, "RIMOV") == 0) // [RIMOV_d***]
        {
            move('R', info_joueur);
        }
        else if(strcmp(message, "IQUIT") == 0) // [IQUIT***]
        {
            read(info_joueur -> sock_tcp, buffer, 3); // ***
            gobye(info_joueur);
            parties[info_joueur -> m].s--;
            parties[info_joueur -> m].joueurs[info_joueur -> i] = NULL;
            if(parties[info_joueur -> m].s == 0)
            {
                reset_game(info_joueur -> m);
                n--;
                printf("La partie est terminée, il n'y a plus aucun joueurs dedans.\n");
            }
            else
            {
                printf("Le joueur %s a quitté la partie.\n", info_joueur -> id);
            }
            return;
        }
        else if(strcmp(message, "GLIS?") == 0) // [GLIS?***]
        {
            read(info_joueur -> sock_tcp, buffer, 3); // ***
            glis(info_joueur);
        }
        else if(strcmp(message, "MALL?") == 0) // [MALL?_mess***]
        {
            mall(info_joueur);
        }
        else if(strcmp(message, "SEND?") == 0) // [SEND?_id_mess***]
        {
            send_mess(info_joueur);
        }
        else
        {
            perror("Requête reçue inattendue (attendu : [UPMOV_d***]/[DOMOV_d***]/[LEMOV_d***]/[RIMOV_d***]/[IQUIT***]/[GLIS?***]/[MALL?_mess***]/[SEND?_id_mess***]).\n");
        }
        if(0 >= parties[info_joueur -> m].f)
        {
            endga(info_joueur);
        }
        if(rand() % 2 == 1)
        {
            deplacer_fantomes_aleatoirement(info_joueur);
        }
    }
}

void move(char d, Player* info_joueur)
{
    char buffer[4], mess[4];
    int is_move_finished = 1;
    

    recv(info_joueur -> sock_tcp, buffer, 1, 0);
    recv(info_joueur -> sock_tcp, mess, 3, 0);
    printf("Message reçu: %s\n", mess);
    mess[3] = '\0';
    uint16_t depl=atoi(mess);
    printf("Deplacement demandé: %d\n", depl);

    uint16_t y=atoi(info_joueur->y);
    uint16_t x=atoi(info_joueur->x);

    recv(info_joueur -> sock_tcp, buffer, 3, 0);
    char stck[4];


    switch (d){
        case 'U':
            for(uint16_t i = y ; i >= y - depl ; i--) {
                if(parties[info_joueur -> m].plateau[i - 1][x]==1 || i - 1 < 0) {
                    printf("test %d, %d\n", i, parties[info_joueur -> m].plateau[i - 1][x]);
                    uint16_to_len_str(stck, i, 3);
                    strcpy(info_joueur->y, stck);
                    is_move_finished = 0;
                    break;
                }
            }
            if(is_move_finished) {
                uint16_to_len_str(stck, y - depl, 3);
                strcpy(info_joueur -> y, stck);
            }
        break;
        case 'D':
            for(uint16_t i = y ; i <= y + depl ; i++) {
                if(parties[info_joueur -> m].plateau[i + 1][x]==1 || i + 1 >= parties[info_joueur -> m].h){
                    printf("test %d, %d\n", i, parties[info_joueur -> m].plateau[i - 1][x]);
                    uint16_to_len_str(stck, i, 3);
                    strcpy(info_joueur -> y, stck);
                    is_move_finished = 0;
                    break;
                }
            }
            if(is_move_finished) {
                uint16_to_len_str(stck, y + depl, 3);
                strcpy(info_joueur -> y, stck);
            }
        break;
        case 'L':
            for(uint16_t i = x ; i >= x - depl ; i--) {
                if(parties[info_joueur -> m].plateau[y][i - 1]==1 || i - 1 < 0){
                    printf("test %d, %d\n", i, parties[info_joueur -> m].plateau[i - 1][x]);
                    uint16_to_len_str(stck, i, 3);
                    strcpy(info_joueur -> x, stck);
                    is_move_finished = 0;
                    break;
                }
            }
            if(is_move_finished) {
                uint16_to_len_str(stck, x - depl, 3);
                strcpy(info_joueur -> x, stck);
            }
        break;
        case 'R':
            for(uint16_t i = x ; i <= x + depl ; i++) {
                if(parties[info_joueur -> m].plateau[y][i + 1]==1 || i + 1 >= parties[info_joueur -> m].l){
                    printf("test %d, %d\n", i, parties[info_joueur -> m].plateau[i - 1][x]);
                    uint16_to_len_str(stck, i, 3);
                    strcpy(info_joueur -> x, stck);
                    is_move_finished = 0;
                    break;
                }
            }
            if(is_move_finished) {
                uint16_to_len_str(stck, x + depl, 3);
                strcpy(info_joueur -> x, stck);
            }
        break;  
    }
    char* mess_send = malloc(sizeof(char)*16);
    sprintf(mess_send, "MOVE! %s %s***", info_joueur -> x, info_joueur -> y);
    printf("Message envoyé: %s\n", mess_send);
    send(info_joueur -> sock_tcp, mess_send, 16, 0);
}

void gobye(Player* info_joueur)
{
    // Envoi du message [GOBYE***]
    char gobye[8] = "GOBYE***";
    if(write(info_joueur -> sock_tcp, gobye, 8) == -1)
    {
        perror("Erreur lors de l'envoi du message [GOBYE***].\n");
    }
    printf("Message [GOBYE***] envoyé au joueur.\n");
}

void glis(Player* info_joueur)
{
    // Envoi du message [GLIS!_s***]
    char glis[10] = "GLIS! s***";
    memcpy(glis + 6, &parties[info_joueur -> m].s, sizeof(uint8_t));
    if(write(info_joueur -> sock_tcp, glis, 10) == -1)
    {
        perror("Erreur lors de l'envoi du message [GLIS!_s***].");
        return;
    }

    // Envoi du/des message(s) [GPLYR_id_x_y_p***]
    char gplyr[31] = "GPLYR id...... x.. y.. p....***";
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[info_joueur -> m].joueurs[i] != NULL)
        {
            memcpy(gplyr + 6, info_joueur -> id, strlen(info_joueur -> id));
            memcpy(gplyr + 15, info_joueur -> x, strlen(info_joueur -> x));
            memcpy(gplyr + 19, info_joueur -> y, strlen(info_joueur -> y));
            memcpy(gplyr + 23, info_joueur -> p, strlen(info_joueur -> p));
            if(write(info_joueur -> sock_tcp, gplyr, 30))
            {
                perror("Erreur lors de l'envoi du message [GPLYR_id_x_y_p***].\n");
                return;
            }
            printf("Message [GPLYR_id_x_y_p***] envoyé au joueur (id = %s, x = %s, y = %s, p = %s).\n", info_joueur -> id, info_joueur -> x, info_joueur -> y, info_joueur -> p);
        }
    }
}

void mall(Player* info_joueur)
{
    char buffer[3], mess[201];
    int read_size;

    // Réception du message [MALL?_mess***]
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read_size = read(info_joueur -> sock_tcp, mess, 204); // mess***
    mess[read_size - 3] = '\0';

    // Vérification que la partie actuelle est toujours en cours
    if(parties[info_joueur -> m].etat != 2)
    {
        // Envoi du message [GOBYE***]
        gobye(info_joueur);
        return;
    }

    // Envoi du message [MESSA_id_mess+++] en multicast
    char messa[219];
    sprintf(messa, "MESSA %s %s+++",info_joueur->id, mess);
    printf("%s",messa);
    if(sendto(parties[info_joueur -> m].sock, messa, strlen(messa), 0, parties[info_joueur -> m].saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
    {
        perror("Erreur lors de l'envoi en multicast du message [MESSA_id_mess+++].\n");
        return;
    }
    printf("Message [MESSA_id_mess+++] envoyé en multicast avec succès (id = \"%s\", mess = \"%s\").\n", info_joueur -> id, mess);
   
   // Envoi du message [MALL!***]
    char mall[9] = "MALL!***"; // [MALL!***]
    if(write(info_joueur -> sock_tcp, mall, 8) == -1)
    {
        perror("Erreur lors de l'envoi du message [MALL!***].\n");
        return;
    }
    printf("Message [MALL!***] envoyé au joueur.\n");
}

void send_mess(Player* info_joueur) // [SEND?_id_mess***]
{
    char buffer[3], id[9], mess[201];
    int read_size;

    // Réception de la requête [SEND?_id_mess***]
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read(info_joueur -> sock_tcp, id, 8); // id
    read(info_joueur -> sock_tcp, buffer, 1); // _
    read_size = read(info_joueur -> sock_tcp, mess, 203); // mess***
    mess[read_size - 3] = '\0';
    id[8] = '\0';
    printf("Requête reçue (id = \"%s\", mess = \"%s\").\n", id, mess);
    
    // Envoi du message [SEND?_id2_mess+++]
    char messp[219]; 
    sprintf(messp, "SEND? %s %s+++", info_joueur->id,mess);
    for(uint8_t i = 0; i < parties[info_joueur->m].s; i++)
    {
        printf("id joueurs = %s\n", parties[info_joueur -> m].joueurs[i] -> id);
        if(parties[info_joueur -> m].joueurs[i] != NULL && strcmp(parties[info_joueur -> m].joueurs[i] -> id, id) == 0)
        {
            printf("id message = %s\n", id);
            printf("PARTIE: sock udp = %s adresse ip = %s\n", parties[info_joueur -> m].port,parties[info_joueur -> m].ip);
            printf("JOUEURS: sock udp = %s\n", parties[info_joueur -> m].joueurs[i] -> port);
            if(sendto(parties[info_joueur -> m].joueurs[i] -> sock_udp, messp, strlen(messp), 0, parties[info_joueur -> m].joueurs[i] -> saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
            {
                perror("Erreur lors de l'envoi en multicast du message [SEND?_id2_mess+++].\n");
                return;
            }
            printf("Message envoyé: %s\n",messp);
            
            // Envoi du message [SEND!***]
            char send2[9] = "SEND!***";
            if(send(info_joueur -> sock_tcp, send2, 8, 0) == -1)
            {
                perror("Erreur lors de l'envoi du message [SEND!***].\n");
                return;
            }
            printf("Message [SEND!***] envoyé au joueur.\n");
            return;
        }
    }
    
    // Envoi du message [NSEND***]
    char nsend[9] = "NSEND***";
    if(write(info_joueur -> sock_tcp, nsend, 8) == -1)
    {
        perror("Erreur lors de l'envoi du message [NSEND***].\n");
        return;
    }
    printf("Message [NSEND***] envoyé au joueur.\n");
}

void endga(Player* info_joueur) // [ENDGA_id_p+++]
{
    Player* gagnant = get_winner(info_joueur);

    // Envoi du message [ENDGA_id_p+++] en multicast
    char endga[21] = "ENDGA id...... p..+++";
    memcpy(endga + 6, gagnant -> id, strlen(gagnant -> id));
    memcpy(endga + 15, gagnant -> p, strlen(gagnant -> p));
    if(sendto(info_joueur -> sock_udp, endga, 9, 0, info_joueur -> saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
    {
        perror("Erreur lors de l'envoi en multicast du message [ENDGA_id_p+++].\n");
        return;
    }
    printf("Message [ENDGA_id_p+++] envoyé au joueur (id = %s, p = %s).\n", gagnant -> id, gagnant -> p);
}

void deplacer_fantomes_aleatoirement(Player* info_joueur)
{
    uint16_t x, y;
    char x_string[4], y_string[4], ghost[16] = "GHOST x.. y..+++";
    for(uint16_t i = 0; i < parties[info_joueur -> m].f; i++)
    {
        do
        {
            x = rand() % parties[info_joueur -> m].l;
            y = rand() % parties[info_joueur -> m].h;
        } while(parties[info_joueur -> m].plateau[x][y] != ESPACE_VIDE);
        parties[info_joueur -> m].plateau[x][y] = FANTOME;
        uint16_to_len_str(x_string, x, 3);
        uint16_to_len_str(y_string, y, 3);

        // Envoi du message [GHOST_x_y_+++] en multicast 
        memcpy(ghost + 6, x_string, strlen(x_string));
        memcpy(ghost + 10, y_string, strlen(y_string));
        if(sendto(info_joueur -> sock_udp, ghost, 16, 0, info_joueur -> saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
        {
            perror("Erreur lors de l'envoi en multicast du message [GHOST_x_y_+++].\n");
            return;
        }
        printf("Message [GHOST_x_y_+++] envoyé en multicast.\n");
    }
}

int is_lobby_ready(uint8_t m)
{
    int a = 0;
    for(int i = 0; i < 255; i++)
    {
        if(parties[m].joueurs[i] != NULL && parties[m].joueurs[i] -> etat == 2)
        {
            a++;
        }
    }
    printf("Nombre de joueurs (prêts) : %d (%d).\n", parties[m].s, a);
    return a > 0 && a == parties[m].s;
}

Player* get_first_player(Player* info_joueur)
{
    for(uint8_t i = 0; i < 255; i++)
    {
        if(parties[info_joueur -> m].joueurs[i] != NULL)
        {
            return parties[info_joueur -> m].joueurs[i];
        }
    }
    return NULL;
}

Player* get_winner(Player* info_joueur)
{
    Player* gagnant;
    uint8_t tmp = 0;
    for(uint8_t i = 0; i < parties[info_joueur -> m].s; i++)
    {
        if(parties[info_joueur -> m].joueurs[i] != NULL && atoi(parties[info_joueur -> m].joueurs[i] -> p) > tmp)
        {
            tmp = atoi(parties[info_joueur -> m].joueurs[i] -> p);
            gagnant = parties[info_joueur -> m].joueurs[i];
        }
    }
    return gagnant;
}

void uint16_to_len_str(char* dest, uint16_t nombre, uint8_t len)
{
    char nombre_string[len];
    uint8_t i;
    for(i = 0; i < len; i++)
    {
        dest[i] = '0';
    }
    dest[i + 1] = '\0';
    sprintf(nombre_string, "%d", nombre);
    memcpy(dest + len - strlen(nombre_string), nombre_string, sizeof(uint16_t));
    printf("Le nombre %u est devenu %s.\n", nombre, dest);
}


void initialize_game() 
{
    for(int i=0;i<255;i++)
    {
        parties[i].ip=malloc(sizeof(char)*16);
        
        parties[i].port=malloc(sizeof(char)*5);
        if(parties[i].ip==NULL || parties[i].port==NULL)
        {
            perror("Erreur lors de l'allocation de la mémoire.\n");
            exit(EXIT_FAILURE);
        }
        parties[i].sock=0; 
        parties[i].saddr=NULL;
        parties[i].etat=0;
        parties[i].f=FANTOMES_DEFAUT;
        parties[i].s=0;
        parties[i].l=LARGEUR_DEFAUT;
        parties[i].h=HAUTEUR_DEFAUT; 
        for(int j=0;j<255;j++){
            parties[i].joueurs[j]=NULL;
        }
    }
}

void reset_game(uint8_t m){

    parties[m].ip = malloc(sizeof(char) * 16); 
    parties[m].port = malloc(sizeof(char) * 5);
    if(parties[m].ip == NULL || parties[m].port == NULL){
        perror("Erreur lors de l'allocation de la mémoire.\n");
        exit(EXIT_FAILURE);
    }
    parties[m].sock = 0; 
    parties[m].saddr = NULL;
    parties[m].etat = 0;
    parties[m].f = 0;
    parties[m].s = 0;
    parties[m].l = LARGEUR_DEFAUT;
    parties[m].h = HAUTEUR_DEFAUT; 
    for(int j = 0; j < 255; j++)
    {
        parties[m].joueurs[j] = NULL;
    }
}

int** parse_txt(char* filename)
{
    FILE* f = fopen(filename, "r");
    if(f == NULL) {
        perror("Erreur lors de l'ouverture du fichier.\n");
        fclose(f);
        exit(1);
    }
    int** lines = malloc(sizeof(int*) * 255);
    int i = 0;
    char* buffer=malloc(sizeof(char) * 255);
    while(fgets(buffer, 255, f) != NULL) {
        lines[i] = malloc(sizeof(int) * 255);
        for(int j = 0; j < 255; j++) {
            lines[i][j] = buffer[j] - '0';
        }
        i++;
    }
    fclose(f);
    free(buffer);
    return lines;
}


int main(int argc, char* argv[])
{
    initialize_game();

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
        if(sock2 >= 0)
        {
            printf("Connexion TCP acceptée.\n");
            srand(time(NULL));
            pthread_create(&th, NULL, hub, (void*) &sock2);
        }
        else
        {
            perror("Connexion échouée.\n");
        }
    }
    return 0;
}