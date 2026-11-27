// javac ChaseAndEvade.java
// java ChaseAndEvade

import java.util.Random;
import java.util.Scanner;

public class ChaseAndEvade
{
    private static final int WIDTH = 90;
    private static final int HEIGHT = 20;
    private static final char EMPTY = ' ';
    private static final char OBSTACLE = '.';
    private static final char CHASER = 'C';
    private static final char EVADER = 'E';

    private char[][] grid = new char[HEIGHT][WIDTH];
    private Random random = new Random();
    private int chaserX, chaserY;
    private int evaderX, evaderY;
    private int chaserSpeed;
    private int chaserSteps;

    public ChaseAndEvade()
    {
        initializeGrid();
        placeObstacles();
        placeCharacters();
        chaserSpeed = 2;
        chaserSteps = 0;
    }

    // Initializes the grid with empty spaces
    private void initializeGrid()
    {
        for (int i = 0; i < HEIGHT; i++)
        {
            for (int j = 0; j < WIDTH; j++)
            {
                grid[i][j] = EMPTY;
            }
        }
    }

    // Places random obstacles in the grid
    private void placeObstacles()
    {
        int obstacleCount = (WIDTH * HEIGHT) / 20; // Sparse obstacles
        for (int i = 0; i < obstacleCount; i++)
        {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            grid[y][x] = OBSTACLE;
        }
    }

    // Places the chaser and evader randomly on the grid
    private void placeCharacters()
    {
        do
        {
            chaserX = random.nextInt(WIDTH);
            chaserY = random.nextInt(HEIGHT);
        } while (grid[chaserY][chaserX] != EMPTY);

        do
        {
            evaderX = random.nextInt(WIDTH);
            evaderY = random.nextInt(HEIGHT);
        } while (grid[evaderY][evaderX] != EMPTY || (evaderX == chaserX && evaderY == chaserY));

        grid[chaserY][chaserX] = CHASER;
        grid[evaderY][evaderX] = EVADER;
    }

    // Moves the characters based on complex AI
    private void update()
    {
        if( chaserSteps % chaserSpeed == 0 )
        {
            grid[chaserY][chaserX] = EMPTY;
            grid[evaderY][evaderX] = EMPTY;

            moveChaser();
            moveEvader();

            grid[chaserY][chaserX] = CHASER;
            grid[evaderY][evaderX] = EVADER;
        }
        else
        {
            grid[chaserY][chaserX] = EMPTY;

            moveChaser();

            grid[chaserY][chaserX] = CHASER;
        }

        chaserSteps++;
    }

    // Chaser moves toward the evader while avoiding obstacles
    private void moveChaser()
    {
        int bestX = chaserX;
        int bestY = chaserY;
        double bestDistance = distance(chaserX, chaserY, evaderX, evaderY);

        // Check all 8 possible directions (including diagonals)
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dy = -1; dy <= 1; dy++)
            {
                if (dx == 0 && dy == 0) continue; // Skip staying in place

                int newX = wrap(chaserX + dx, WIDTH);
                int newY = wrap(chaserY + dy, HEIGHT);

                if (grid[newY][newX] == EMPTY) // Avoid obstacles
                {
                    double newDistance = distance(newX, newY, evaderX, evaderY);
                    if (newDistance < bestDistance)
                    {
                        bestX = newX;
                        bestY = newY;
                        bestDistance = newDistance;
                    }
                }
            }
        }

        chaserX = bestX;
        chaserY = bestY;
    }

    // Evader moves away from the chaser while avoiding obstacles
    private void moveEvader()
    {
        int bestX = evaderX;
        int bestY = evaderY;
        double bestDistance = distance(evaderX, evaderY, chaserX, chaserY);

        // Check all 8 possible directions (including diagonals)
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dy = -1; dy <= 1; dy++)
            {
                if (dx == 0 && dy == 0) continue; // Skip staying in place

                int newX = wrap(evaderX + dx, WIDTH);
                int newY = wrap(evaderY + dy, HEIGHT);

                if (grid[newY][newX] == EMPTY) // Avoid obstacles
                {
                    double newDistance = distance(newX, newY, chaserX, chaserY);
                    if (newDistance > bestDistance)
                    {
                        bestX = newX;
                        bestY = newY;
                        bestDistance = newDistance;
                    }
                }
            }
        }

        evaderX = bestX;
        evaderY = bestY;
    }

    // Helper method to calculate the Toroidal-Euclidean distance between two points
    private double distance(int x1, int y1, int x2, int y2)
    {
        // Euclidean distance is smallest, or left wrap is smallest, or right wrap is smallest?
        int dx = Math.min(Math.abs(x1 - x2), Math.min(Math.abs(x1 - 0) + Math.abs(x2 - WIDTH), Math.abs(x1 - WIDTH) + Math.abs(x2 - 0)) );

        // Euclidean distance is smallest, or bottom wrap is smallest, or top wrap is smallest?
        int dy = Math.min(Math.abs(y1 - y2), Math.min(Math.abs(y1 - 0) + Math.abs(y2 - HEIGHT), Math.abs(y1 - HEIGHT) + Math.abs(y2 - 0)) );

        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist;
    }

    // Wraps the position around the grid (toroidal space)
    private int wrap(int coord, int max)
    {
        if (coord < 0) return max - 1;
        if (coord >= max) return 0;
        return coord;
    }

    // Renders the grid to the console
    private void renderGrid()
    {
        for (int i = 0; i < HEIGHT; i++)
        {
            for (int j = 0; j < WIDTH; j++)
            {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
        System.out.println("C: Chaser, E: Evader");
    }

    // Main game loop
    public void runGame()
    {
        Scanner scanner = new Scanner(System.in);
        while (true)
        {
            renderGrid();
            update();
            try
            {
                Thread.sleep(300);
            }
            catch(InterruptedException e)
            {
                    System.out.println( e.getMessage() );
            }
        }
    }

    public static void main(String[] args)
    {
        ChaseAndEvade game = new ChaseAndEvade();
        game.runGame();
    }
}