all: server client

server: server.c
	gcc -pthread -Wall -g server.c -o server

client: Client.java
	javac Client.java

labyrinthe: labyrinthe.java
	javac labyrinthe.java

clean:
	rm server Client.class