import java.util.LinkedList;
import java.util.Random;

public class labyrinthe{





    static char[][] create_maze_grid(int w,int l){
        int length=2*l+4;
        int width=2*w+4;
        char[][] maze=new char[length][width];
        int i,j;
        for(i=2;i<length-2;i++){
            for(j=2;j<width-2;j++){
                if(i%2!=0){
                    if(j%2==0){
                        maze[i][j]='|';
                    }
                    else{
                        maze[i][j]='.';
                    }
                }
                else{
                    maze[i][j]='-';
                }
            }
        }
        for(i=0;i<length;i++){
            maze[i][0]='?';
            maze[i][width-1]='?';
        }
        for(i=0;i<length;i++){
            maze[i][1]='?';
            maze[i][width-2]='?';
        }
        for(j=0;j<width;j++){
            maze[0][j]='?';
            maze[length-1][j]='?';
        }
        for(j=0;j<width;j++){
            maze[1][j]='?';
            maze[length-2][j]='?';
        }
        return maze;
    }
    

    static char[][] create_maze_rec(char[][] maze,int pos_x,int pos_y,LinkedList<Integer> last_direction){
        maze[pos_y][pos_x]=' ';

        /*for(int i=0;i<last_direction.size();i++){
            System.out.print(last_direction.get(i));
        }
        System.out.println();*/
        int nb_adj_walls=0;
        int[] adj_cells={0,0,0,0};

   
            while(maze[pos_y-2][pos_x]=='.' || maze[pos_y][pos_x+2]=='.' || maze[pos_y+2][pos_x]=='.' || maze[pos_y][pos_x-2]=='.'){
                if(maze[pos_y-2][pos_x]=='.'){
                    nb_adj_walls++;
                    adj_cells[0]=1;
                }
                if(maze[pos_y][pos_x+2]=='.'){
                    nb_adj_walls++;
                    adj_cells[1]=1;
                }
                if(maze[pos_y+2][pos_x]=='.'){
                    nb_adj_walls++;
                    adj_cells[2]=1;
                }
                if(maze[pos_y][pos_x-2]=='.'){
                    nb_adj_walls++;
                    adj_cells[3]=1;
                }
            

                
            
                if(nb_adj_walls!=0){
                    
                    int adj_walls_index[]=new int[nb_adj_walls];
                    for(int i=0;i<nb_adj_walls;i++){
                        for(int j=0;j<4;j++){
                            if(adj_cells[j]==1){
                                adj_walls_index[i]=j;
                                adj_cells[j]=0;
                                break;
                            }
                        }
                    }

                    Random rand=new Random();
                    int r=rand.nextInt(nb_adj_walls);
                    int deplacement=adj_walls_index[r];
                    nb_adj_walls=0;

                    //print_maze(maze);

                    switch (deplacement){
                        case 0:
                            /*if(maze[pos_y-1][pos_x]=='?'){
                                maze[pos_y-1][pos_x]=' ';
                                return maze;
                            }*/
                            if(pos_y>3){
                                maze[pos_y-1][pos_x]=' ';
                                last_direction.addLast(0);
                                return create_maze_rec(maze,pos_x,pos_y-2,last_direction);
                            }
                            break;
                        case 1:
                            /*if(maze[pos_y][pos_x+1]=='?'){
                                maze[pos_y][pos_x+1]=' ';
                                return maze;
                            }*/
                            if(pos_x<maze[0].length-3){
                                maze[pos_y][pos_x+1]=' ';
                                last_direction.addLast(1);
                                return create_maze_rec(maze,pos_x+2,pos_y,last_direction);
                            }
                            break;
                        case 2:
                            /*if(maze[pos_y+1][pos_x]=='?'){
                                maze[pos_y+1][pos_x]=' ';
                                return maze;
                            }*/
                            if(pos_y<maze.length-3){
                                maze[pos_y+1][pos_x]=' ';
                                last_direction.addLast(2);
                                return create_maze_rec(maze,pos_x,pos_y+2,last_direction);
                            }
                            break;
                        case 3:
                            /*if(maze[pos_y][pos_x-1]=='?'){
                                maze[pos_y][pos_x-1]=' ';
                                return maze;
                            }*/
                            if(pos_x>3){
                                maze[pos_y][pos_x-1]=' ';
                                last_direction.addLast(3);
                                return create_maze_rec(maze,pos_x-2,pos_y,last_direction);
                            }
                            break;
                    }
                    
                }
            }
            if(!last_direction.isEmpty()){
                switch(last_direction.getLast()){
                    case 0:
                        last_direction.removeLast();
                        return create_maze_rec(maze,pos_x,pos_y+2,last_direction);
                    case 1:
                        last_direction.removeLast();
                        return create_maze_rec(maze,pos_x-2,pos_y,last_direction);
                    case 2:
                        last_direction.removeLast();
                        return create_maze_rec(maze,pos_x,pos_y-2,last_direction);
                    case 3:
                        last_direction.removeLast();
                        return create_maze_rec(maze,pos_x+2,pos_y,last_direction);
                }
            }
    
        return maze;
    }


    static void print_maze(char[][] maze){
        int i,j;
    
        System.out.println("========================================================");
   
        for(i=2;i<maze.length-2;i++){
            for(j=2;j<maze[i].length-2;j++){
                System.out.print(maze[i][j]);
            }
            if(i%2==0){
                System.out.println("-");
            }
            else{
                System.out.println("|");
            }
        }
        for(i=2;i<maze.length-2;i++){
            System.out.print("-");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        char[][] maze=create_maze_grid(8,8);
        LinkedList<Integer> last_direction=new LinkedList<Integer>();
        maze=create_maze_rec(maze,3,3,last_direction);
        print_maze(maze);
    }

}