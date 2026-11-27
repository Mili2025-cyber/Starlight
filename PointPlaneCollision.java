//javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointPlaneCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" PointPlaneCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class PointPlaneCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    // Point properties
    private float[] point = {-10.0f, 0.0f, 0.0f}; // Initial position on the left
    private float[] velocity = {0.1f, 0.0f, 0.0f}; // Initial velocity to move slowly to the right
    private float gravity = 0.0f; // No gravity since the motion is horizontal

    // Plane properties
    private float[] planeNormal = {0.0f, 1.0f, 0.0f}; // Normal pointing up, horizontal plane
    private float[] planePoint = {0.0f, -1.0f, 0.0f}; // Plane slightly below the point's path

    // Thresholds
    private float minimumVelocityThreshold = 0.01f; // Minimum velocity to avoid rattling
    private float collisionOffset = 0.1f; // Small offset to push the point away from the plane after collision

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

        window = glfwCreateWindow(width, height, "Bouncing Point 3D", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Initial conditions
        point[0] = -10.0f;  // Start on the left side of the screen
        point[1] = 5.0f;    // Start above the plane
        point[2] = 0.0f;    // No initial depth (Z-axis)

        velocity[0] = 0.1f; // Move to the right slowly
        velocity[1] = 0.0f; // Initially, no vertical velocity
        gravity = -0.01f;   // Apply a small constant gravity
    }

    private void loop()
    {
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

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

        // Update point position
        point[0] += velocity[0];  // Horizontal motion
        point[1] += velocity[1];  // Vertical motion
        point[2] += velocity[2];  // No change in Z for now

        if (point[1] <= 0.0f) // Collision detection when the point is at or below the plane
        {
            // Reflect the vertical velocity to simulate a bounce
            velocity[1] = -velocity[1] * 0.8f; // Bounce with some energy loss

            // Correct the point's position to stay above the plane
            point[1] = 0.0f;
        }

        // Apply a small horizontal friction to slow down over time
        velocity[0] *= 0.99f;
    }

    private void renderScene()
    {
        // Set up the camera
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f, (float) width / height, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, -2.0f, -20.0f); // Move the camera back and slightly down

        // Render the stationary plane
        glPushMatrix();
        glTranslatef(planePoint[0], planePoint[1], planePoint[2]);

        glColor3f(0.0f, 0.0f, 1.0f); // Blue plane
        glBegin(GL_QUADS);
        glVertex3f(-10.0f, 0.0f, -10.0f);
        glVertex3f(10.0f, 0.0f, -10.0f);
        glVertex3f(10.0f, 0.0f, 10.0f);
        glVertex3f(-10.0f, 0.0f, 10.0f);
        glEnd();
        glPopMatrix();

        // Render the point moving from left to right
        glColor3f(1.0f, 0.0f, 0.0f); // Red point
        glPointSize(10.0f);
        glBegin(GL_POINTS);
        glVertex3f(point[0], point[1], point[2]);
        glEnd();
    }

    private void gluPerspective(float fovY, float aspect, float zNear, float zFar)
    {
        float fH = (float) Math.tan(fovY / 360.0 * Math.PI) * zNear;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, zNear, zFar);
    }

    public static void main(String[] args)
    {
        new PointPlaneCollision().run();
    }
}