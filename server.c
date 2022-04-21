#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <signal.h>

#define MAX_ID 8

struct user_info
{
    int sock_tcp;
    int sock_udp;
    char* tag;
    uint16_t max;
    uint32_t sin_addr;
};

void* comm(void* sock2);

int main(int argc, char* argv[])
{
    // Déclaration des variables
    int port_tcp, port_udp, sock_tcp, sock_udp, sock2, sock3, size;
    struct sockaddr_in sockaddress_tcp, sockaddress_udp, caller;
    struct user_info info;
    pthread_t th;

    // Parsing de la ligne de commandes
    if(3 != argc)
    {
        printf("UTILISATION\n./serveur port_tcp\n");
        return 0;
    }
    port_tcp = atoi(argv[1]);
    port_udp = atoi(argv[2]);
    printf("Ligne de commande parsée.\n");

    // Création du socket TCP
    sock_tcp = socket(PF_INET, SOCK_STREAM, 0);
    if(sock_tcp == -1)
    {
        printf("Erreur lors de la création du socket.\n");
        return 1;
    }
    sockaddress_tcp.sin_family = AF_INET;
    sockaddress_tcp.sin_port = htons(port_tcp);
    sockaddress_tcp.sin_addr.s_addr = htonl(INADDR_ANY);
    printf("Socket TCP créée.\n");

    // Création du socket UDP
    sock_udp = socket(PF_INET, SOCK_STREAM, 0);
    if(sock_udp == -1)
    {
        printf("Erreur lors de la création du socket.\n");
        return 1;
    }
    sockaddress_udp.sin_family = AF_INET;
    sockaddress_udp.sin_port = htons(port_udp);
    sockaddress_udp.sin_addr.s_addr = htonl(INADDR_ANY);
    printf("Socket UDP créée.\n");


    // Binding
    if(bind(sock_tcp, (struct sockaddr *) &sockaddress_tcp, sizeof(struct sockaddr_in)) < 0) // TODO: ??? sizeof
    {
        perror("Erreur lors du binding du serveur avec la socket TCP.\n");
        return 1;
    }
    if(bind(sock_udp, (struct sockaddr *) &sockaddress_udp, sizeof(struct sockaddr_in)) < 0) // TODO: ??? sizeof
    {
        perror("Erreur lors du binding du serveur avec la socket UDP.\n");
        return 1;
    }
    printf("Binding fait.\n");

    // Listen
    if(listen(sock_tcp, 3) != 0 && listen(sock_udp, 3) != 0)
    {
        perror("Erreur lors de la création du serveur.\n");
        return 1;
    }
    printf("En attente d'une connexion...\n");
    
    size = sizeof(struct sockaddr_in);
    while(1)
    {
        sock2 = accept(sock_tcp, (struct sockaddr*) &caller, (socklen_t*) &size);
        sock3 = accept(sock_udp, (struct sockaddr*) &caller, (socklen_t*) &size);
        printf("Connexion acceptée.\n");
        info.sock_tcp = sock2;
        info.sock_udp = sock3;
        info.sin_addr = caller.sin_addr.s_addr;
        if(pthread_create(&th, NULL, comm, (void*) &info) < 0)
        {
            perror("Erreur lors de la création du thread.\n");
            return 1;
        }
    }
    if(sock2 < 0)
    {
        perror("Connexion échouée.\n");
        return 1;
    }
    return 0;
}

void* comm(void* info)
{
    // Déclaration des variables
    struct user_info* current_user = (struct user_info*) info;
    int sock_tcp = current_user -> sock_tcp;
    int sock_udp = current_user -> sock_udp;
    int read_size;
    uint16_t number;
    char client_message[2000], answer[17], tag[16];
    struct sockaddr *saddr = current_user -> ai_addr;
     
    while(1)
    {
        // Lecture d'un message TCP
        printf("Je cherche à lire un message en TCP.\n");
        read_size = read(sock_tcp, tag, 99 * sizeof(char));
        tag[read_size] = '\0';

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
        printf("Message reçu en TCP : '%s'\n", tag);
    
        // Réponse en TCP
        memcpy(answer, tag, strlen(tag));
        memcpy(answer + strlen(tag) - 1, "feur", strlen("feur"));
        answer[16] = '\0';
        if(write(sock_tcp, answer, strlen(answer)) == -1)
        {
            perror("Erreur lors du hello au pseudo reçu.\n");
            return NULL;
        }

        // Témoin
        printf("Réponse envoyée en TCP : '%s'\n", answer);

        // Lecture d'un message UDP
        printf("Je cherche à lire un message en UDP.\n");
        read_size = recv(sock_udp, tag, 99 * sizeof(char), 0);
        tag[read_size] = '\0';

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
        printf("Message reçu en UDP : '%s'\n", tag);
    
        // Réponse en UDP
        memcpy(answer, tag, strlen(tag));
        memcpy(answer + strlen(tag) - 1, "feur", strlen("feur"));
        answer[16] = '\0';
        if(sendto(sock_udp, answer, strlen(answer), 0, saddr, (socklen_t) sizeof(struct sockaddr_in)) == -1)
        {
            perror("Erreur lors du hello au pseudo reçu.\n");
            return NULL;
        }

        // Témoin
        printf("Réponse envoyée en UDP : '%s'\n", answer);

    }
    return 0;
}
