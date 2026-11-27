// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointLineCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointLineCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class PointLineCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    private float[] point = {100.0f, 500.0f};  // Initial position of the point
    private float[] velocity = {3.0f, 4.0f};   // Velocity of the point
    private float gravity = -0.1f;             // Gravity affecting the point

    private float[] lineStart = {400.0f, 300.0f};  // Center of the spinning line
    private float lineLength = 200.0f;             // Length of the line
    private float angle = 0.0f;                    // Initial angle of the line (in radians)
    private float angularVelocity = 0.01f;         // Angular velocity of the spinning line

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

        window = glfwCreateWindow(width, height, "Point-Line Intersection and Elastic Collision", NULL, NULL);
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

            updateMotion();
            renderScene();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updateMotion()
    {
        // Update point's velocity with gravity
        velocity[1] += gravity;

        // Update point's position based on velocity
        point[0] += velocity[0];
        point[1] += velocity[1];

        // Update the line's angle to simulate spinning
        angle += angularVelocity;

        // Calculate line endpoints based on the current angle
        float[] lineEnd1 = {
            lineStart[0] + (float) Math.cos(angle) * lineLength / 2,
            lineStart[1] + (float) Math.sin(angle) * lineLength / 2
        };
        float[] lineEnd2 = {
            lineStart[0] - (float) Math.cos(angle) * lineLength / 2,
            lineStart[1] - (float) Math.sin(angle) * lineLength / 2
        };

        // Check for collision between the point and the line
        if (pointIntersectsLine(point, lineEnd1, lineEnd2))
        {
            // Calculate the line's normal vector
            float[] lineNormal = {
                lineEnd2[1] - lineEnd1[1],
                lineEnd1[0] - lineEnd2[0]
            };
            normalize(lineNormal);

            // Calculate the dot product between velocity and line normal
            float dotProduct = velocity[0] * lineNormal[0] + velocity[1] * lineNormal[1];

            // Apply a minimum velocity threshold to avoid rattling
            float minVelocityThreshold = 0.5f; // Adjust this as necessary
            if (Math.abs(dotProduct) < minVelocityThreshold)
            {
                // Absorb shallow collisions: Stop further motion along the normal
                velocity[0] -= dotProduct * lineNormal[0];
                velocity[1] -= dotProduct * lineNormal[1];
            }
            else
            {
                // Reflect the point's velocity over the line's normal vector
                velocity[0] -= 2 * dotProduct * lineNormal[0];
                velocity[1] -= 2 * dotProduct * lineNormal[1];

                // Adjust the point's position slightly after collision to avoid immediate re-collision
                point[0] += lineNormal[0] * minVelocityThreshold;
                point[1] += lineNormal[1] * minVelocityThreshold;
            }
        }

        // Keep point within window bounds
        checkBounds(point, velocity);
    }

    private boolean pointIntersectsLine(float[] p, float[] l1, float[] l2)
    {
        // Calculate the distance from the point to the infinite line
        float distance = Math.abs((l2[1] - l1[1]) * p[0] - (l2[0] - l1[0]) * p[1] + l2[0] * l1[1] - l2[1] * l1[0])
            / (float) Math.sqrt(Math.pow(l2[1] - l1[1], 2) + Math.pow(l2[0] - l1[0], 2));

        // Check if the point is within the segment
        float dotProduct1 = (p[0] - l1[0]) * (l2[0] - l1[0]) + (p[1] - l1[1]) * (l2[1] - l1[1]);
        float dotProduct2 = (p[0] - l2[0]) * (l1[0] - l2[0]) + (p[1] - l2[1]) * (l1[1] - l2[1]);

        return distance < 5.0f && dotProduct1 >= 0 && dotProduct2 >= 0; // Ensure the point is on the segment
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

    private void renderScene()
    {
        // Calculate line endpoints based on the current angle
        float[] lineEnd1 = {
            lineStart[0] + (float) Math.cos(angle) * lineLength / 2,
            lineStart[1] + (float) Math.sin(angle) * lineLength / 2
        };
        float[] lineEnd2 = {
            lineStart[0] - (float) Math.cos(angle) * lineLength / 2,
            lineStart[1] - (float) Math.sin(angle) * lineLength / 2
        };

        // Render the line
        renderLine(lineEnd1, lineEnd2, 1.0f, 1.0f, 1.0f); // White line

        // Render the point
        renderPoint(point[0], point[1], 1.0f, 0.0f, 0.0f); // Red point
    }

    private void renderLine(float[] start, float[] end, float r, float g, float b)
    {
        glColor3f(r, g, b);
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        glVertex2f(2.0f * start[0] / width - 1.0f, 2.0f * start[1] / height - 1.0f);
        glVertex2f(2.0f * end[0] / width - 1.0f, 2.0f * end[1] / height - 1.0f);
        glEnd();
    }

    private void renderPoint(float x, float y, float r, float g, float b)
    {
        glColor3f(r, g, b);
        glPointSize(5.0f);
        glBegin(GL_POINTS);
        glVertex2f(2.0f * x / width - 1.0f, 2.0f * y / height - 1.0f);
        glEnd();
    }

    private void normalize(float[] vector)
    {
        float length = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
        vector[0] /= length;
        vector[1] /= length;
    }

    public static void main(String[] args)
    {
        new PointLineCollision().run();
    }
}