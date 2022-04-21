all: server client

server: server.c
	gcc -pthread -Wall server.c -o server

client: Client.java
	javac Client.java

clean:
	rm server Client.class