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


int main(int argc, char* argv[]) {
    // Initialize the server 
    server_init();
    // Start the server
    server_start();
    // Cleanup the server
    server_cleanup();
    return 0;
}

// mutithread tcp server
int connect(){
    int sockfd, new_fd;  // listen on sock_fd, new connection on new_fd
    struct sockaddr_in my_addr;    // my address information
    struct sockaddr_in their_addr; // connector's address information
    socklen_t sin_size;
    struct sigaction sa;
    int yes=1;
    char s[INET_ADDRSTRLEN];
    int rv;

    // Create a socket
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("socket");
        exit(1);
    }

    // Set socket options
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1) {
        perror("setsockopt");
        exit(1);
    }

    // Set up the address structure
    my_addr.sin_family = AF_INET;         // host byte order
    my_addr.sin_port = htons(4244);       // short, network byte order
    my_addr.sin_addr.s_addr = INADDR_ANY; // automatically fill with my IP
    memset(&(my_addr.sin_zero), '\0', 8); // zero the rest of the struct

    // Bind the socket
    if (bind(sockfd, (struct sockaddr *)&my_addr, sizeof(struct sockaddr)) == -1) {
        perror("bind");
        exit(1);
    }

    // Listen for connections
    if (listen(sockfd, 8) == -1) {
        perror("listen");
        exit(1);
    }

    // Accept a connection
    sin_size = sizeof(struct sockaddr_in);
    if ((new_fd = accept(sockfd, (struct sockaddr *)&their_addr, &sin_size)) == -1) {
        perror("accept");
        exit(1);
    }

    // Get the IP address of the client
   // inet_ntop(their_addr
}



int main() {
    int sock=socket(PF_INET,SOCK_STREAM,0);
    struct sockaddr_in sockaddress;
    sockaddress.sin_family=AF_INET;
    sockaddress.sin_port=htons(4244);
    sockaddress.sin_addr.s_addr=htonl(INADDR_ANY);
    int r=bind(sock,(struct sockaddr *)&sockaddress,sizeof(struct sockaddr_in));
    if(r==0){
        r=listen(sock,0);
        if(r==0){
            struct sockaddr_in caller;
            socklen_t size=sizeof(caller);
            while(1){
                int *sock2=(int *)malloc(sizeof(int));
                *sock2=accept(sock,(struct sockaddr *)&caller,&size);
                if(sock2>=0){
                    pthread_t th;
                    pthread_create(&th,NULL,comm,sock2);
                }
            }
        }
    }
    return 0;
}


