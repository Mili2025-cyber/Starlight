// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" LinePlaneCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" LinePlaneCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LinePlaneCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    // Falling line properties
    private float[] fallingLineStart = {4.0f, 5.0f};
    private float[] fallingLineEnd = {5.0f, 5.0f};
    private float[] velocity = {-0.02f, -0.05f};  // Horizontal and vertical velocities
    private float gravity = -0.01f;  // Stronger gravity to simulate a realistic fall
    private float angle = 0.0f;
    private float angularVelocity = 0.05f;  // Rotation speed

    // Plane properties
    private float[] planeColor = {0.0f, 0.0f, 1.0f}; // Blue plane

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

        window = glfwCreateWindow(width, height, "Falling Rotating Line on 3D Plane", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void loop()
    {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
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

        // Apply rotation and gravity to update the falling line's endpoints
        fallingLineStart[0] = centerX + halfLength * (float) Math.cos(angle);
        fallingLineStart[1] = centerY + halfLength * (float) Math.sin(angle) + velocity[1];
        fallingLineEnd[0] = centerX - halfLength * (float) Math.cos(angle);
        fallingLineEnd[1] = centerY - halfLength * (float) Math.sin(angle) + velocity[1];

        // Move the center horizontally from right to left
        centerX += velocity[0];

        // Update the positions again after the horizontal movement
        fallingLineStart[0] = centerX + halfLength * (float) Math.cos(angle);
        fallingLineStart[1] = centerY + halfLength * (float) Math.sin(angle) + velocity[1];
        fallingLineEnd[0] = centerX - halfLength * (float) Math.cos(angle);
        fallingLineEnd[1] = centerY - halfLength * (float) Math.sin(angle) + velocity[1];

        // Check for collision with the plane (Y = 0)
        if (fallingLineStart[1] <= 0 || fallingLineEnd[1] <= 0)
        {
            // Elastic bounce: reverse vertical velocity to simulate a bounce
            velocity[1] = -velocity[1] * 0.8f; // Reverse and reduce slightly for energy loss
            angularVelocity *= 0.9f; // Reduce angular velocity to simulate energy loss

            // Adjust the position to ensure it doesn't go below the plane
            if (fallingLineStart[1] < 0) fallingLineStart[1] = 0;
            if (fallingLineEnd[1] < 0) fallingLineEnd[1] = 0;
        }

        // Ensure the line settles when motion becomes negligible
        if (Math.abs(velocity[1]) < 0.001f && Math.abs(angularVelocity) < 0.001f)
        {
            velocity[1] = 0;
            angularVelocity = 0;
        }
    }

    private void renderScene()
    {
        // Set up the camera
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f, (float) width / height, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, -2.0f, -10.0f); // Adjusted to ensure the plane is visible

        // Render the blue plane
        glColor3f(0.0f, 0.0f, 1.0f); // Blue color
        glBegin(GL_QUADS);
        glVertex3f(-5.0f, 0.0f, -5.0f);
        glVertex3f(5.0f, 0.0f, -5.0f);
        glVertex3f(5.0f, 0.0f, 5.0f);
        glVertex3f(-5.0f, 0.0f, 5.0f);
        glEnd();

        // Render the falling, rotating red line
        glColor3f(1.0f, 0.0f, 0.0f); // Red color
        glLineWidth(5.0f);
        glBegin(GL_LINES);
        glVertex3f(fallingLineStart[0], fallingLineStart[1], 0.0f);
        glVertex3f(fallingLineEnd[0], fallingLineEnd[1], 0.0f);
        glEnd();
    }

    private void gluPerspective(float fov, float aspect, float zNear, float zFar)
    {
        float ymax = zNear * (float) Math.tan(Math.toRadians(fov / 2));
        float xmax = ymax * aspect;
        glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
    }

    public static void main(String[] args)
    {
        new LinePlaneCollision().run();
    }
}