// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" RopePhysics.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" RopePhysics

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RopePhysics
{
    private long window;
    private int width = 800;
    private int height = 600;

    public static void main(String[] args)
    {
        new RopePhysics().run();
    }

    public void run()
    {
        init();
        loop();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private void init()
    {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit())
        {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = GLFW.glfwCreateWindow(width, height, "Rope Simulation", 0, 0);
        if (window == 0)
        {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GL11.glViewport(0, 0, width, height);
    }

    private void loop()
    {
        Rope rope = new Rope(10, 400, 300, window);  // Rope with 10 segments

        while (!GLFW.glfwWindowShouldClose(window))
        {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glLoadIdentity();
            GL11.glOrtho(0, width, height, 0, -1, 1);

            rope.update();
            rope.render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }
}

class Particle
{
    public float x, y;
    public float vx = 0, vy = 0;
    public float mass = 1.0f;
    public boolean isFixed;

    public Particle(float x, float y, boolean isFixed)
    {
        this.x = x;
        this.y = y;
        this.isFixed = isFixed;
    }

    public void applyForce(float fx, float fy)
    {
        if (!isFixed)
        {
            vx += fx / mass;
            vy += fy / mass;
        }
    }

    public void update(float deltaTime)
    {
        if (!isFixed)
        {
            vy += 9.81f * deltaTime;  // Gravity
            x += vx * deltaTime;
            y += vy * deltaTime;
        }
    }
}

class Constraint
{
    private Particle p1, p2;
    private float restLength;

    public Constraint(Particle p1, Particle p2, float restLength)
    {
        this.p1 = p1;
        this.p2 = p2;
        this.restLength = restLength;
    }

    public Particle getP1()
    {
        return p1;
    }

    public Particle getP2()
    {
        return p2;
    }

    public void satisfyConstraint()
    {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float diff = (dist - restLength) / dist;

        if (!p1.isFixed)
        {
            p1.x += dx * diff * 0.5f;
            p1.y += dy * diff * 0.5f;
        }
        if (!p2.isFixed)
        {
            p2.x -= dx * diff * 0.5f;
            p2.y -= dy * diff * 0.5f;
        }
    }
}

class Rope
{
    private List<Particle> particles = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();
    private Particle bottomParticle;
    private long window;

    // Variables to store if a key is currently pressed to simulate burst hits
    private boolean leftKeyPressed = false;
    private boolean rightKeyPressed = false;
    private boolean upKeyPressed = false;
    private boolean downKeyPressed = false;

    public Rope(int segmentCount, float startX, float startY, long window) {
        this.window = window;

        // Create particles
        for (int i = 0; i < segmentCount; i++)
        {
            boolean isFixed = (i == 0);  // First particle is fixed (top end)
            Particle particle = new Particle(startX, startY + i * 20, isFixed);
            particles.add(particle);

            if (i > 0)
            {
                Particle prev = particles.get(i - 1);
                constraints.add(new Constraint(prev, particle, 20));
            }
        }
        bottomParticle = particles.get(particles.size() - 1);
    }

    public void update()
    {
        float deltaTime = 0.016f;  // Assuming 60 FPS

        // Update physics
        for (Particle particle : particles)
        {
            particle.update(deltaTime);
        }

        // Apply constraints
        for (int i = 0; i < 10; i++)
        {
            // Relax constraints several times
            for (Constraint constraint : constraints)
            {
                constraint.satisfyConstraint();
            }
        }

        // Apply a burst force only once when a key is initially pressed
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS && !leftKeyPressed)
        {
            bottomParticle.applyForce(-500, 0);  // Strong impulse to the left
            leftKeyPressed = true;  // Mark that left key was pressed
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_RELEASE)
        {
            leftKeyPressed = false;  // Reset on key release
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS && !rightKeyPressed)
        {
            bottomParticle.applyForce(500, 0);  // Strong impulse to the right
            rightKeyPressed = true;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_RELEASE)
        {
            rightKeyPressed = false;
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS && !upKeyPressed)
        {
            bottomParticle.applyForce(0, -500);  // Strong impulse upwards
            upKeyPressed = true;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_RELEASE)
        {
            upKeyPressed = false;
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS && !downKeyPressed)
        {
            bottomParticle.applyForce(0, 500);  // Strong impulse downwards
            downKeyPressed = true;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_RELEASE)
        {
            downKeyPressed = false;
        }

        // Apply damping to the velocity to simulate friction or air resistance
        applyDamping();
    }

    // Method to apply damping (slows down the velocity over time)
    private void applyDamping()
    {
        for (Particle particle : particles)
        {
            particle.vx *= 0.99f;  // Reduce velocity slightly each frame
            particle.vy *= 0.99f;
        }
    }

    public void render()
    {
        GL11.glBegin(GL11.GL_LINES);
        for (Constraint constraint : constraints)
        {
            GL11.glVertex2f(constraint.getP1().x, constraint.getP1().y);
            GL11.glVertex2f(constraint.getP2().x, constraint.getP2().y);
        }
        GL11.glEnd();
    }
}