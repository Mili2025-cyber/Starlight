// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" SpherePlaneCollision.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" SpherePlaneCollision

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SpherePlaneCollision
{
    private long window;
    private int width = 800;
    private int height = 600;

    // Sphere properties
    private float[] spherePos = {3.0f, 5.0f}; // Initial position of the sphere
    private float[] sphereVelocity = {-0.03f, -0.05f}; // Initial horizontal and vertical velocities
    private float gravity = -0.01f; // Gravity affecting the sphere
    private float radius = 0.5f; // Radius of the sphere
    private float angularVelocity = 0.0f; // Angular velocity for rolling effect
    private float rotationAngle = 0.0f; // Rotation angle of the sphere

    // Plane properties
    private float planeWidth = 6.0f;
    private float planeHeight = 1.0f;

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

        window = glfwCreateWindow(width, height, "Bouncing and Rolling Sphere", NULL, NULL);
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
    sphereVelocity[1] += gravity;

    // Update the position of the sphere
    spherePos[0] += sphereVelocity[0];
    spherePos[1] += sphereVelocity[1];

    // Check for collision with the plane (Y = radius) if the sphere is still on the plane
    if (spherePos[1] - radius <= 0.0f &&
        spherePos[0] - radius >= -planeWidth / 2 &&
        spherePos[0] + radius <= planeWidth / 2)
    {
        spherePos[1] = radius; // Ensure the sphere stays on the plane

        // Reverse and reduce vertical velocity for a smaller bounce
        sphereVelocity[1] = -sphereVelocity[1] * 0.5f;

        // If the vertical velocity is very low, simulate rolling instead of bouncing
        if (Math.abs(sphereVelocity[1]) < 0.02f)
        {
            sphereVelocity[1] = 0;  // Stop bouncing

            // Maintain horizontal velocity but convert it into rolling motion
            angularVelocity = sphereVelocity[0] / radius; // Continue rolling
        }
    }

    // Continue rolling if vertical velocity is zero
    if (sphereVelocity[1] == 0)
    {
        angularVelocity = sphereVelocity[0] / radius;
    }

    // Update the rotation angle based on angular velocity
    rotationAngle += angularVelocity;

    // If the sphere is beyond the edge of the plane, apply gravity and let it fall off
    if (spherePos[0] - radius < -planeWidth / 2 || spherePos[0] + radius > planeWidth / 2)
    {
        sphereVelocity[1] += gravity; // Continue applying gravity to make the sphere fall
        sphereVelocity[0] *= 0.99f;   // Apply slight friction to horizontal motion
    }
}

    private void renderScene() {
        // Set up the camera
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f, (float) width / height, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, -1.0f, -10.0f); // Adjust the camera to view the scene

        // Render the blue plane
        glColor3f(0.0f, 0.0f, 1.0f); // Blue color
        glBegin(GL_QUADS);
        glVertex3f(-planeWidth / 2, 0.0f, -planeHeight);
        glVertex3f(planeWidth / 2, 0.0f, -planeHeight);
        glVertex3f(planeWidth / 2, 0.0f, planeHeight);
        glVertex3f(-planeWidth / 2, 0.0f, planeHeight);
        glEnd();

        // Render the red sphere
        glColor3f(1.0f, 0.0f, 0.0f); // Red color
        renderSphere(spherePos[0], spherePos[1], radius);
    }

private void renderSphere(float x, float y, float r) {
    int latitudeBands = 30;
    int longitudeBands = 30;

    glPushMatrix();
    glTranslatef(x, y, 0.0f);

    // Apply the rotation based on the accumulated rotation angle
    glRotatef(rotationAngle, 0.0f, 0.0f, 1.0f);  // Rotate around the Z-axis for a proper rolling effect

    for (int latNumber = 0; latNumber <= latitudeBands; latNumber++) {
        float theta = latNumber * (float) Math.PI / latitudeBands;
        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);

        glBegin(GL_TRIANGLE_STRIP);
        for (int longNumber = 0; longNumber <= longitudeBands; longNumber++) {
            float phi = longNumber * 2 * (float) Math.PI / longitudeBands;
            float sinPhi = (float) Math.sin(phi);
            float cosPhi = (float) Math.cos(phi);

            float xPos = cosPhi * sinTheta;
            float yPos = cosTheta;
            float zPos = sinPhi * sinTheta;

            glVertex3f(r * xPos, r * yPos, r * zPos);
        }
        glEnd();
    }

    glPopMatrix();
}

    private void gluPerspective(float fov, float aspect, float zNear, float zFar) {
        float ymax = zNear * (float) Math.tan(Math.toRadians(fov / 2));
        float xmax = ymax * aspect;
        glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
    }

    public static void main(String[] args) {
        new SpherePlaneCollision().run();
    }
}