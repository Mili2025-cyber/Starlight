// javac FiniteStateMachines.java
// java FiniteStateMachines

import java.util.Random;
import java.util.Scanner;

class StatBlock
{
    int strength, dexterity, constitution, intelligence, wisdom, charisma;
    int health;

    public StatBlock()
    {
        Random random = new Random();
        this.strength = rollStat(random);
        this.dexterity = rollStat(random);
        this.constitution = rollStat(random);
        this.intelligence = rollStat(random);
        this.wisdom = rollStat(random);
        this.charisma = rollStat(random);
        this.health = 10 + getModifier(constitution); // Base health + Con modifier
    }

    private int rollStat(Random random)
    {
        return random.nextInt(15) + 3; // Roll between 3 and 18
    }

    private int getModifier(int stat)
    {
        return (stat - 10) / 2;
    }

    public int attack()
    {
        Random random = new Random();
        return random.nextInt(20) + 1 + getModifier(strength);
    }

    public void takeDamage(int damage)
    {
        this.health -= damage;
    }

    public boolean isAlive()
    {
        return this.health > 0;
    }

    @Override
    public String toString()
    {
        return "Str: " + strength + ", Dex: " + dexterity + ", Con: " + constitution +
               ", Int: " + intelligence + ", Wis: " + wisdom + ", Cha: " + charisma +
               ", HP: " + health;
    }
}

public class FiniteStateMachines
{
    enum GameState
    {
        START, PLAY, GAME_OVER
    }

    private GameState currentState;
    private StatBlock player;
    private StatBlock enemy;
    private int score;

    public FiniteStateMachines()
    {
        currentState = GameState.START;
        score = 0;
    }

    public void start()
    {
        Scanner scanner = new Scanner(System.in);
        while (true)
        {
            switch (currentState)
            {
                case START:
                    System.out.println("Welcome to the DnD ASCII Game!");
                    System.out.println("Press 'p' to play or 'q' to quit.");
                    String input = scanner.nextLine();
                    if (input.equals("p"))
                    {
                        initializePlayer();
                        currentState = GameState.PLAY;
                    }
                    else if (input.equals("q"))
                    {
                        System.exit(0);
                    }
                    break;

                case PLAY:
                    if (enemy == null || !enemy.isAlive())
                    {
                        spawnNewEnemy();
                    }
                    System.out.println("\nPlayer: " + player);
                    System.out.println("Enemy: " + enemy);
                    System.out.println("Choose an action: 'a' to attack, 'q' to quit.");
                    input = scanner.nextLine();
                    if (input.equals("a"))
                    {
                        performAttack();
                        if (!player.isAlive())
                        {
                            currentState = GameState.GAME_OVER;
                        }
                    }
                    else if (input.equals("q"))
                    {
                        System.exit(0);
                    }
                    break;

                case GAME_OVER:
                    System.out.println("Game Over! You defeated " + score + " enemies.");
                    System.out.println("Press 'r' to restart or 'q' to quit.");
                    input = scanner.nextLine();
                    if (input.equals("r"))
                    {
                        currentState = GameState.START;
                        score = 0;
                    }
                    else if (input.equals("q"))
                    {
                        System.exit(0);
                    }
                    break;
            }
        }
    }

    private void initializePlayer()
    {
        player = new StatBlock();
        System.out.println("Your character's stat block: " + player);
    }

    private void spawnNewEnemy()
    {
        enemy = new StatBlock();
        System.out.println("\nA new enemy approaches with stat block: " + enemy);
    }

    private void performAttack()
    {
        int playerAttack = player.attack();
        int enemyAttack = enemy.attack();

        System.out.println("You attack with a roll of " + playerAttack);
        System.out.println("Enemy attacks with a roll of " + enemyAttack);

        if (playerAttack > enemyAttack)
        {
            int damage = Math.max(playerAttack - enemyAttack, 0);
            enemy.takeDamage(damage);
            System.out.println("You hit the enemy for " + damage + " damage.");
            if (!enemy.isAlive())
            {
                System.out.println("Enemy defeated!");
                score++;
                enemy = null;
            }
        }
        else
        {
            int damage = Math.max(enemyAttack - playerAttack, 0);
            player.takeDamage(damage);
            System.out.println("Enemy hits you for " + damage + " damage.");
        }

        if (!player.isAlive())
        {
            System.out.println("You have been slain!");
        }
    }

    public static void main(String[] args)
    {
        FiniteStateMachines game = new FiniteStateMachines();
        game.start();
    }
}