// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointPointCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointPointCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class PointPointCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    private float[] point1 = {100.0f, 500.0f};  // Initial position of the first point
    private float[] point2 = {700.0f, 500.0f};  // Initial position of the second point

    private float[] velocity1 = {3.0f, 4.0f};   // Velocity of the first point
    private float[] velocity2 = {-3.0f, 4.0f};  // Velocity of the second point

    private float gravity = -0.1f; // Gravity affecting the y-axis

    public void run()
    {
        init();
        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init()
    {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Point Intersection and Elastic Collision", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void loop()
    {
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        while (!glfwWindowShouldClose(window))
        {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            updatePoints();
            renderPoints();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updatePoints()
    {
        // Update velocities with gravity
        velocity1[1] += gravity;
        velocity2[1] += gravity;

        // Update positions based on velocities
        point1[0] += velocity1[0];
        point1[1] += velocity1[1];
        point2[0] += velocity2[0];
        point2[1] += velocity2[1];

        // Check for collision (simple point intersection)
        if (Math.abs(point1[0] - point2[0]) < 1.0f && Math.abs(point1[1] - point2[1]) < 1.0f)
        {
            // Swap velocities for a perfect elastic collision
            float[] tempVelocity = velocity1.clone();
            velocity1 = velocity2;
            velocity2 = tempVelocity;
        }

        // Keep points within window bounds
        checkBounds(point1, velocity1);
        checkBounds(point2, velocity2);
    }

    private void checkBounds(float[] point, float[] velocity)
    {
        if (point[0] < 0 || point[0] > width)
        {
            velocity[0] = -velocity[0];
        }
        if (point[1] < 0 || point[1] > height)
        {
            velocity[1] = -velocity[1];
        }
    }

    private void renderPoints()
    {
        // Render the first point
        renderPoint(point1[0], point1[1], 1.0f, 0.0f, 0.0f); // Red color

        // Render the second point
        renderPoint(point2[0], point2[1], 0.0f, 0.0f, 1.0f); // Blue color
    }

    private void renderPoint(float x, float y, float r, float g, float b)
    {
        glColor3f(r, g, b);
        glPointSize(5.0f);
        glBegin(GL_POINTS);
        glVertex2f(2.0f * x / width - 1.0f, 2.0f * y / height - 1.0f);
        glEnd();
    }

    public static void main(String[] args)
    {
        new PointPointCollision().run();
    }
}