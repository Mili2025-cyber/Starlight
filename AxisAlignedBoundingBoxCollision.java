// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" AxisAlignedBoundingBoxCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" AxisAlignedBoundingBoxCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class AxisAlignedBoundingBoxCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    // Box properties
    private float[] box1Pos = {-2.0f, 3.0f}; // Initial position of the first box
    private float[] box2Pos = {2.0f, 3.0f};  // Initial position of the second box
    private float[] boxVelocity1 = {0.02f, 0.1f}; // Velocity of the first box
    private float[] boxVelocity2 = {-0.02f, 0.1f}; // Velocity of the second box
    private float gravity = -0.01f; // Gravity affecting both boxes
    private float boxSize = 1.0f;   // Size of each box

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

        window = glfwCreateWindow(width, height, "Bounding Box Collision Demo", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

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
        boxVelocity1[1] += gravity;
        boxVelocity2[1] += gravity;

        // Update the positions of the boxes
        box1Pos[0] += boxVelocity1[0];
        box1Pos[1] += boxVelocity1[1];

        box2Pos[0] += boxVelocity2[0];
        box2Pos[1] += boxVelocity2[1];

        // Check for collision between the two boxes using AABB
        if (checkCollision(box1Pos, box2Pos, boxSize))
        {
            // Elastic collision: reverse both horizontal and vertical velocities
            boxVelocity1[0] = -boxVelocity1[0];
            boxVelocity1[1] = -boxVelocity1[1];
            boxVelocity2[0] = -boxVelocity2[0];
            boxVelocity2[1] = -boxVelocity2[1];
        }

        // Check for ground collision and make the boxes bounce back up
        if (box1Pos[1] - boxSize / 2 <= 0.0f)
        {
            box1Pos[1] = boxSize / 2; // Prevent the box from going below the ground
            boxVelocity1[1] = -boxVelocity1[1]; // Bounce back up
        }

        if (box2Pos[1] - boxSize / 2 <= 0.0f)
        {
            box2Pos[1] = boxSize / 2; // Prevent the box from going below the ground
            boxVelocity2[1] = -boxVelocity2[1]; // Bounce back up
        }
    }

    private boolean checkCollision(float[] pos1, float[] pos2, float size)
    {
        return (pos1[0] < pos2[0] + size && pos1[0] + size > pos2[0] &&
                pos1[1] < pos2[1] + size && pos1[1] + size > pos2[1]);
    }

    private void renderScene()
    {
        // Set up the camera
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f, (float) width / height, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, -2.0f, -10.0f); // Adjust the camera to view the scene

        // Render the first box (white)
        glColor3f(1.0f, 1.0f, 1.0f); // White color
        renderWireframeBox(box1Pos[0], box1Pos[1], boxSize);

        // Render the second box (light blue)
        glColor3f(0.5f, 0.8f, 1.0f); // Light blue color
        renderWireframeBox(box2Pos[0], box2Pos[1], boxSize);
    }

    private void renderWireframeBox(float x, float y, float size)
    {
        glPushMatrix();
        glTranslatef(x, y, 0.0f);

        glBegin(GL_LINE_LOOP);
        // Front face
        glVertex3f(-size / 2, -size / 2, size / 2);
        glVertex3f(size / 2, -size / 2, size / 2);
        glVertex3f(size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, size / 2, size / 2);
        glEnd();

        glBegin(GL_LINE_LOOP);
        // Back face
        glVertex3f(-size / 2, -size / 2, -size / 2);
        glVertex3f(size / 2, -size / 2, -size / 2);
        glVertex3f(size / 2, size / 2, -size / 2);
        glVertex3f(-size / 2, size / 2, -size / 2);
        glEnd();

        glBegin(GL_LINES);
        // Connect front and back faces
        glVertex3f(-size / 2, -size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, -size / 2);

        glVertex3f(size / 2, -size / 2, size / 2);
        glVertex3f(size / 2, -size / 2, -size / 2);

        glVertex3f(size / 2, size / 2, size / 2);
        glVertex3f(size / 2, size / 2, -size / 2);

        glVertex3f(-size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, size / 2, -size / 2);
        glEnd();

        glPopMatrix();
    }

    private void gluPerspective(float fov, float aspect, float zNear, float zFar)
    {
        float ymax = zNear * (float) Math.tan(Math.toRadians(fov / 2));
        float xmax = ymax * aspect;
        glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
    }

    public static void main(String[] args)
    {
        new AxisAlignedBoundingBoxCollision().run();
    }
}