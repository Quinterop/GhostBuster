all: server client labyrinthe comm multi

server: server.c
	gcc -pthread -Wall -g server.c -o server

client: Client.java
	javac Client.java

labyrinthe: labyrinthe.java
	javac labyrinthe.java

comm: Communication.java
	javac Communication.java

multi: CommMulticast.java
	javac CommMulticast.java

clean:
	rm server *.class