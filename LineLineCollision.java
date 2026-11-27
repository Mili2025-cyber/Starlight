// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" LineLineCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" LineLineCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LineLineCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    // Falling line properties
    private float[] fallingLineStart = {300.0f, 500.0f};
    private float[] fallingLineEnd = {500.0f, 500.0f};
    private float[] velocity = {-0.5f, -0.2f};  // Slower horizontal and vertical velocities
    private float gravity = -0.01f;  // Slower falling
    private float angle = 0.0f;
    private float angularVelocity = 0.005f;  // Slower and smoother rotation

    // Stationary line properties
    private float[] stationaryLineStart = {200.0f, 100.0f};
    private float[] stationaryLineEnd = {600.0f, 100.0f};

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

        window = glfwCreateWindow(width, height, "Falling Rotating Line", NULL, NULL);
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
        // Apply gravity to vertical velocity
        velocity[1] += gravity;

        // Calculate the center of the falling line before updating its position
        float centerX = (fallingLineStart[0] + fallingLineEnd[0]) / 2;
        float centerY = (fallingLineStart[1] + fallingLineEnd[1]) / 2;

        // Update rotation angle
        angle += angularVelocity;

        // Calculate half length of the line
        float halfLength = (float) Math.sqrt(Math.pow(fallingLineEnd[0] - fallingLineStart[0], 2) + Math.pow(fallingLineEnd[1] - fallingLineStart[1], 2)) / 2;

        // Update the falling line's endpoints based on rotation and center
        fallingLineStart[0] = centerX + halfLength * (float) Math.cos(angle);
        fallingLineStart[1] = centerY + halfLength * (float) Math.sin(angle);
        fallingLineEnd[0] = centerX - halfLength * (float) Math.cos(angle);
        fallingLineEnd[1] = centerY - halfLength * (float) Math.sin(angle);

        // Apply linear motion to the center
        centerX += velocity[0];
        centerY += velocity[1];

        fallingLineStart[0] = centerX + halfLength * (float) Math.cos(angle);
        fallingLineStart[1] = centerY + halfLength * (float) Math.sin(angle);
        fallingLineEnd[0] = centerX - halfLength * (float) Math.cos(angle);
        fallingLineEnd[1] = centerY - halfLength * (float) Math.sin(angle);

        // Check for collision with the stationary line
        if (isLineIntersecting(fallingLineStart, fallingLineEnd, stationaryLineStart, stationaryLineEnd))
        {
            // Elastic bounce: reverse vertical velocity to simulate a bounce
            velocity[1] = -velocity[1] * 0.8f; // Reverse and reduce slightly for energy loss

            // Increase angular velocity to simulate rotation due to bounce
            angularVelocity *= 1.1f;

            // Immediately adjust the position to prevent the line from getting stuck
            fallingLineStart[1] += Math.abs(velocity[1]) * 0.1f;
            fallingLineEnd[1] += Math.abs(velocity[1]) * 0.1f;
        }
        else
        {
            // Continue falling if the line is past the stationary line's endpoints
            if (fallingLineEnd[0] < stationaryLineStart[0] || fallingLineStart[0] > stationaryLineEnd[0])
            {
                velocity[1] += gravity;  // Continue accelerating downward
            }
        }
    }

    private boolean isLineIntersecting(float[] line1Start, float[] line1End, float[] line2Start, float[] line2End)
    {
        // Simple axis-aligned bounding box (AABB) collision detection
        float minX1 = Math.min(line1Start[0], line1End[0]);
        float maxX1 = Math.max(line1Start[0], line1End[0]);
        float minY1 = Math.min(line1Start[1], line1End[0]);
        float maxY1 = Math.max(line1Start[1], line1End[1]);

        float minX2 = Math.min(line2Start[0], line2End[0]);
        float maxX2 = Math.max(line2Start[0], line2End[0]);
        float minY2 = Math.min(line2Start[1], line2End[0]);
        float maxY2 = Math.max(line2Start[1], line2End[1]);

        return (minX1 <= maxX2 && maxX1 >= minX2) && (minY1 <= maxY2 && maxY1 >= minY2);
    }

    private void renderScene()
    {
        // Set up the camera
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0.0f, width, 0.0f, height);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Render stationary line
        glColor3f(1.0f, 1.0f, 1.0f);
        glLineWidth(5.0f);
        glBegin(GL_LINES);
        glVertex2f(stationaryLineStart[0], stationaryLineStart[1]);
        glVertex2f(stationaryLineEnd[0], stationaryLineEnd[1]);
        glEnd();

        // Render falling line
        glPushMatrix();
        glTranslatef((fallingLineStart[0] + fallingLineEnd[0]) / 2, (fallingLineStart[1] + fallingLineEnd[1]) / 2, 0.0f);
        glRotatef((float) Math.toDegrees(angle), 0.0f, 0.0f, 1.0f);
        glTranslatef(-(fallingLineStart[0] + fallingLineEnd[0]) / 2, -(fallingLineStart[1] + fallingLineEnd[1]) / 2, 0.0f);

        glBegin(GL_LINES);
        glVertex2f(fallingLineStart[0], fallingLineStart[1]);
        glVertex2f(fallingLineEnd[0], fallingLineEnd[1]);
        glEnd();

        glPopMatrix();
    }

    private void gluOrtho2D(float left, float right, float bottom, float top)
    {
        glOrtho(left, right, bottom, top, -1.0f, 1.0f);
    }

    public static void main(String[] args)
    {
        new LineLineCollision().run();
    }
}