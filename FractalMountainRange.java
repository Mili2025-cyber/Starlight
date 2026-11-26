// javac FractalMountainRange.java
// java FractalMountainRange

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class FractalMountainRange
{
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MAX_DISPLACEMENT_MOUNTAIN = 150; // Maximum displacement for the mountain peaks
    private static final int MAX_DISPLACEMENT_CLOUD = 80; // Maximum displacement for the cloud peaks
    private static final Random random = new Random();

    public static void main(String[] args)
    {
        // Create an image
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Initialize the mountain and cloud lines
        int[] mountainHeights = new int[WIDTH];
        int[] cloudHeights = new int[WIDTH];

        // Set initial points for the mountain line
        mountainHeights[0] = HEIGHT / 2; // Start of the mountain horizon
        mountainHeights[WIDTH - 1] = HEIGHT / 2; // End of the mountain horizon

        // Set initial points for the cloud line (higher up in the sky)
        cloudHeights[0] = HEIGHT / 4; // Start of the cloud horizon
        cloudHeights[WIDTH - 1] = HEIGHT / 4; // End of the cloud horizon

        // Generate the fractal mountain and cloud lines
        generateFractalLine(mountainHeights, 0, WIDTH - 1, MAX_DISPLACEMENT_MOUNTAIN);
        generateFractalLine(cloudHeights, 0, WIDTH - 1, MAX_DISPLACEMENT_CLOUD);

        // Draw the sky, mountains, and clouds
        drawMountainAndClouds(image, mountainHeights, cloudHeights);

        g.dispose();

        // Save the image to a PNG file
        try
        {
            ImageIO.write(image, "png", new File("fractal_mountain_and_clouds.png"));
            System.out.println("Fractal mountain and clouds image generated and saved as fractal_mountain_and_clouds.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Recursive function to generate a fractal line using midpoint displacement
    private static void generateFractalLine(int[] heights, int start, int end, int displacement)
    {
        if (start + 1 == end) return; // Base case: stop when the points are adjacent

        // Find the midpoint
        int mid = (start + end) / 2;

        // Displace the midpoint vertically
        heights[mid] = (heights[start] + heights[end]) / 2 + random.nextInt(2 * displacement) - displacement;

        // Reduce the displacement factor for finer detail
        int newDisplacement = Math.max(displacement / 2, 1);

        // Recursively apply the same to the left and right halves
        generateFractalLine(heights, start, mid, newDisplacement);
        generateFractalLine(heights, mid, end, newDisplacement);
    }

    // Function to draw the mountain range and clouds on the image
    private static void drawMountainAndClouds(BufferedImage image, int[] mountainHeights, int[] cloudHeights)
    {
        for (int x = 0; x < WIDTH; x++)
        {
            for (int y = 0; y < HEIGHT; y++)
            {
                // Color everything above the cloud line white (clouds)
                if (y < cloudHeights[x])
                {
                    image.setRGB(x, y, new Color(255, 255, 255).getRGB()); // Cloud white
                }
                // Color everything below the cloud line but above the mountain line sky blue
                else if (y < mountainHeights[x])
                {
                    image.setRGB(x, y, new Color(135, 206, 250).getRGB()); // Sky blue
                }
                // Color everything below the mountain line black (mountains)
                else
                {
                    image.setRGB(x, y, new Color(0, 0, 0).getRGB()); // Black
                }
            }
        }
    }
}