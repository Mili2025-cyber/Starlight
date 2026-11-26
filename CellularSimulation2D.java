// javac CellularSimulation2D.java
// java CellularSimulation2D

import java.util.Vector;
import java.util.Random;

public class CellularSimulation2D
{
    public static Vector< Vector< String > > loadGame( String config )
    {
        Vector< Vector< String > > ret = new Vector< Vector< String > >();
        int width = 80;
        int height = 24;
        if( config.equals( "glider" ) )
        {
            for( int i = 0; i < height; i++ )
            {
                Vector< String > tempRow = new Vector< String >();
                for( int j = 0; j < width; j++ )
                    tempRow.add( " " );
                ret.add( tempRow );
            }

            ret.elementAt(0).set(0, "o");
            ret.elementAt(0).set(1, "o");
            ret.elementAt(0).set(2, "o");
            ret.elementAt(1).set(2, "o");
            ret.elementAt(2).set(1, "o");
        }
        else
        {
            for( int i = 0; i < height; i++ )
            {
                Vector< String > tempRow = new Vector< String >();
                for( int j = 0; j < width; j++ )
                    tempRow.add( " " );
                ret.add( tempRow );
            }

            Random rand = new Random();
            for( int i = 0; i < width * height; i++ )
            {
                int iy = rand.nextInt(height);
                int jx = rand.nextInt(width);
                ret.elementAt(iy).set(jx, "o");
            }
        }

        return ret;
    }

    public static Integer checkNbrs( Vector< Vector< String > > gameBoard, Integer row, Integer col )
    {
        Integer ret = 0;
        Integer left = col-1;
        Integer right = col+1;
        Integer top = row-1;
        Integer bot = row+1;

        if( left < 0 )
        {
            left = left + gameBoard.elementAt(0).size();
        }
        if( right >= gameBoard.elementAt(0).size() )
        {
            right = right - gameBoard.elementAt(0).size();
        }
        if( top < 0 )
        {
            top = top + gameBoard.size();
        }
        if( bot >= gameBoard.size() )
        {
            bot = bot - gameBoard.size();
        }

        if( !gameBoard.elementAt(top).elementAt(left).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(top).elementAt(col).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(top).elementAt(right).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(row).elementAt(left).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(row).elementAt(right).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(bot).elementAt(left).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(bot).elementAt(col).equals(" ") )
        {
            ret = ret + 1;
        }
        if( !gameBoard.elementAt(bot).elementAt(right).equals(" ") )
        {
            ret = ret + 1;
        }

        return ret;
    }

    public static Vector< Vector< String > > updateGame( Vector< Vector< String > > gameBoard )
    {
        Vector< Vector< String > > ret = new Vector< Vector< String > >();

        // init ret
        for( int i = 0; i < gameBoard.size(); i++ )
        {
            Vector< String > tempRow = new Vector< String >();
            for( int j = 0; j < gameBoard.elementAt(i).size(); j++ )
            {
                tempRow.add( " " );
            }
            ret.add( tempRow );
        }

        // populate ret based on 4 Game of Life rules.
        // 1. Any live cell with fewer than two live neighbors dies, as if by underpopulation.
        // 2. Any live cell with two or three live neighbors lives on to the next generation.
        // 3. Any live cell with more than three live neighbors dies, as if by overpopulation.
        // 4. Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
        for( int i = 0; i < gameBoard.size(); i++ )
        {
            for( int j = 0; j < gameBoard.elementAt(i).size(); j++ )
            {
                int nbrCnt = checkNbrs( gameBoard, i, j );
                if( !gameBoard.elementAt(i).elementAt(j).equals(" ") ) // live cell
                {
                    if( nbrCnt < 2 )
                    {
                        ret.elementAt(i).set(j, " ");
                    }
                    else if( nbrCnt == 2 || nbrCnt == 3 )
                    {
                        ret.elementAt(i).set(j, "O");
                    }
                    else if( nbrCnt > 3 )
                    {
                        ret.elementAt(i).set(j, " ");
                    }
                }
                else // dead cell
                {
                    if( nbrCnt == 3 )
                    {
                        ret.elementAt(i).set(j, "o");
                    }
                }
            }
        }

        return ret;
    }

    public static void printGame( Vector< Vector< String > > gameBoard )
    {
        System.out.println();
        System.out.print( " +" );
        for(int i = 0; i < gameBoard.elementAt(0).size(); i++)
            System.out.print( "-" );
        System.out.println( "+" );
        for(int i = 0; i < gameBoard.size(); i++)
        {
            System.out.print( " |" );
            String tempRow = new String();
            for(int j = 0; j < gameBoard.elementAt(0).size(); j++)
            {
                tempRow += gameBoard.elementAt(i).elementAt(j);
            }
            System.out.println(tempRow + "|");
        }

        System.out.print( " +" );
        for(int i = 0; i < gameBoard.elementAt(0).size(); i++)
            System.out.print( "-" );
        System.out.println( "+" );
    }

    public static void main(String[] args)
    {
        Vector< Vector< String > > gameBoard = loadGame( "random" );
        while( true )
        {
            printGame( gameBoard );
            gameBoard = updateGame( gameBoard );
            try
            {
                Thread.sleep(250);
            }
            catch( InterruptedException ie )
            {
                ie.printStackTrace();
            }
        }
    }
}