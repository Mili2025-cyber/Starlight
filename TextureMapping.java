// javac TextureMapping.java
// java TextureMapping

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class TextureMapping
{
    public static void main(String[] args) throws IOException
    {
        Scene scene = OBJParser.parse("cube.obj");
        Camera camera = new Camera(new Vector3(0, 0, 3), new Vector3(0, 0, 0), 90);

        // Rotate for 30 degrees around the X-axis
        Matrix4x4 rotationMatrix = RotationUtil.glRotate(30, 1, 0, 0);

        // Apply rotation to the object
        RotationUtil.applyRotation(scene, rotationMatrix);

        // Rotate for 30 degrees around the Y-axis
        rotationMatrix = RotationUtil.glRotate(30, 0, 1, 0);

        // Apply rotation to the object
        RotationUtil.applyRotation(scene, rotationMatrix);

        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage texture = ImageIO.read(new File("texture.png")); // Load texture image

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                double ndcX = (x + 0.5) / width;
                double ndcY = (y + 0.5) / height;
                double screenX = 2 * ndcX - 1;
                double screenY = 1 - 2 * ndcY;
                double aspectRatio = (double) width / height;
                screenX *= aspectRatio;

                Vector3 rayDirection = new Vector3(screenX, screenY, -1).normalize();
                Ray ray = new Ray(camera.position, rayDirection);

                Color color = traceRay(ray, scene, texture);
                image.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(image, "png", new File("output.png"));
    }

    private static Color sampleTexture(BufferedImage texture, Vector2 uv)
    {
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        // Ensure UV coordinates are within the [0, 1] range
        uv.x = Math.max(0, Math.min(uv.x, 1));
        uv.y = Math.max(0, Math.min(uv.y, 1));

        // Convert UV coordinates to texture pixel coordinates
        int texX = (int)(uv.x * (texWidth - 1));
        int texY = (int)(uv.y * (texHeight - 1));

        // Get the color from the texture
        return new Color(texture.getRGB(texX, texY));
    }

    private static Color traceRay(Ray ray, Scene scene, BufferedImage texture)
    {
        Intersection closestIntersection = null;
        Face closestFace = null;

        for (Face face : scene.faces)
        {
            Vector3 v0 = scene.vertices.get(face.vertices[0]);
            Vector3 v1 = scene.vertices.get(face.vertices[1]);
            Vector3 v2 = scene.vertices.get(face.vertices[2]);

            Intersection intersection = RayTracer.intersectRayTriangle(ray, v0, v1, v2);
            if (intersection.hit && (closestIntersection == null || intersection.distance < closestIntersection.distance))
            {
                closestIntersection = intersection;
                closestFace = face;
            }
        }

        if (closestIntersection != null && closestFace != null)
        {
            // Retrieve the correct vertices and UVs for interpolation
            Vector3 v0 = scene.vertices.get(closestFace.vertices[0]);
            Vector3 v1 = scene.vertices.get(closestFace.vertices[1]);
            Vector3 v2 = scene.vertices.get(closestFace.vertices[2]);

            Vector2 uv0 = scene.uvs.get(closestFace.uvs[0]);
            Vector2 uv1 = scene.uvs.get(closestFace.uvs[1]);
            Vector2 uv2 = scene.uvs.get(closestFace.uvs[2]);

            // Calculate barycentric coordinates for the intersection point
            Vector3 barycentricCoords = calculateBarycentricCoordinates(closestIntersection.point, v0, v1, v2);

            // Interpolate the UV coordinates using the barycentric coordinates
            Vector2 interpolatedUV = interpolateUV(barycentricCoords, uv0, uv1, uv2);

            // Adjust the UVs to span the whole face, not just individual triangles
            interpolatedUV = adjustUVForWholeFace(interpolatedUV, scene, closestFace);

            // Sample the texture using the interpolated and adjusted UVs
            return sampleTexture(texture, interpolatedUV);
        }
        else
        {
            return new Color(0, 0, 0); // background color
        }
    }

    private static Vector2 adjustUVForWholeFace(Vector2 interpolatedUV, Scene scene, Face closestFace)
    {
        // Get the UV coordinates of all triangles in the face
        Vector2 uv0 = scene.uvs.get(closestFace.uvs[0]);
        Vector2 uv1 = scene.uvs.get(closestFace.uvs[1]);
        Vector2 uv2 = scene.uvs.get(closestFace.uvs[2]);

        // Find the minimum and maximum UV coordinates for the entire face (bounding box)
        double minU = Math.min(uv0.x, Math.min(uv1.x, uv2.x));
        double maxU = Math.max(uv0.x, Math.max(uv1.x, uv2.x));
        double minV = Math.min(uv0.y, Math.min(uv1.y, uv2.y));
        double maxV = Math.max(uv0.y, Math.max(uv1.y, uv2.y));

        // Normalize the interpolated UV within the face's bounding box
        double normalizedU = (interpolatedUV.x - minU) / (maxU - minU);
        double normalizedV = (interpolatedUV.y - minV) / (maxV - minV);

        // Ensure UV values are clamped between 0 and 1 to avoid out-of-bounds texture lookup
        normalizedU = Math.max(0, Math.min(1, normalizedU));
        normalizedV = Math.max(0, Math.min(1, normalizedV));

        return new Vector2(normalizedU, normalizedV);
    }

    private static Vector3 calculateBarycentricCoordinates(Vector3 p, Vector3 a, Vector3 b, Vector3 c)
    {
        Vector3 v0 = b.subtract(a);
        Vector3 v1 = c.subtract(a);
        Vector3 v2 = p.subtract(a);

        double d00 = v0.dot(v0);
        double d01 = v0.dot(v1);
        double d11 = v1.dot(v1);
        double d20 = v2.dot(v0);
        double d21 = v2.dot(v1);

        double denom = d00 * d11 - d01 * d01;
        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        return new Vector3(u, v, w);
    }

    private static Vector2 interpolateUV(Vector3 barycentricCoords, Vector2 uv0, Vector2 uv1, Vector2 uv2)
    {
        double u = barycentricCoords.x * uv0.x + barycentricCoords.y * uv1.x + barycentricCoords.z * uv2.x;
        double v = barycentricCoords.x * uv0.y + barycentricCoords.y * uv1.y + barycentricCoords.z * uv2.y;
        return new Vector2(u, v);
    }
}

class RotationUtil
{
    // Function to simulate glRotatef, rotating around a specified axis by a certain angle
    public static Matrix4x4 glRotate(double angle, double x, double y, double z)
    {
        double radAngle = Math.toRadians(angle); // Convert angle to radians
        Matrix4x4 rotationMatrix;

        // Normalize the axis vector (x, y, z)
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;

        // If rotating around the X-axis
        if (x == 1 && y == 0 && z == 0)
        {
            rotationMatrix = new Matrix4x4(
                1, 0, 0, 0,
                0, Math.cos(radAngle), -Math.sin(radAngle), 0,
                0, Math.sin(radAngle), Math.cos(radAngle), 0,
                0, 0, 0, 1
            );
        }
        // If rotating around the Y-axis
        else if (x == 0 && y == 1 && z == 0)
        {
            rotationMatrix = new Matrix4x4(
                Math.cos(radAngle), 0, Math.sin(radAngle), 0,
                0, 1, 0, 0,
                -Math.sin(radAngle), 0, Math.cos(radAngle), 0,
                0, 0, 0, 1
            );
        }
        // If rotating around the Z-axis
        else if (x == 0 && y == 0 && z == 1)
        {
            rotationMatrix = new Matrix4x4(
                Math.cos(radAngle), -Math.sin(radAngle), 0, 0,
                Math.sin(radAngle), Math.cos(radAngle), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
            );
        }
        else
        {
            // For more general cases of arbitrary axis rotation, you could implement Rodrigues' rotation formula
            throw new UnsupportedOperationException("Only X, Y, Z axis rotations are supported");
        }
        return rotationMatrix;
    }

    // Apply the rotation matrix to each vertex of the object
    public static void applyRotation(Scene scene, Matrix4x4 rotationMatrix)
    {
        for (int i = 0; i < scene.vertices.size(); i++)
        {
            Vector3 rotatedVertex = rotationMatrix.multiply(scene.vertices.get(i));
            scene.vertices.set(i, rotatedVertex);  // Update each vertex with the rotated value
        }
    }
}

class Matrix4x4
{
    private double[][] m;  // 4x4 matrix

    // Constructor for an empty matrix
    public Matrix4x4()
    {
        m = new double[4][4];
    }

    // Constructor for a matrix with initial values
    public Matrix4x4(double m00, double m01, double m02, double m03,
                     double m10, double m11, double m12, double m13,
                     double m20, double m21, double m22, double m23,
                     double m30, double m31, double m32, double m33)
    {
        m = new double[4][4];
        m[0][0] = m00; m[0][1] = m01; m[0][2] = m02; m[0][3] = m03;
        m[1][0] = m10; m[1][1] = m11; m[1][2] = m12; m[1][3] = m13;
        m[2][0] = m20; m[2][1] = m21; m[2][2] = m22; m[2][3] = m23;
        m[3][0] = m30; m[3][1] = m31; m[3][2] = m32; m[3][3] = m33;
    }

    // Matrix multiplication with another Matrix4x4
    public Matrix4x4 multiply(Matrix4x4 other)
    {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                result.m[i][j] = 0;
                for (int k = 0; k < 4; k++)
                {
                    result.m[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return result;
    }

    // Multiply the matrix with a Vector3 (for 3D transformations)
    public Vector3 multiply(Vector3 v)
    {
        double x = v.x * m[0][0] + v.y * m[1][0] + v.z * m[2][0] + m[3][0];
        double y = v.x * m[0][1] + v.y * m[1][1] + v.z * m[2][1] + m[3][1];
        double z = v.x * m[0][2] + v.y * m[1][2] + v.z * m[2][2] + m[3][2];
        return new Vector3(x, y, z);
    }

    // Static function to create an identity matrix
    public static Matrix4x4 identity()
    {
        return new Matrix4x4(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        );
    }

    // Static function to create a rotation matrix around the X-axis
    public static Matrix4x4 rotationX(double angle)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Matrix4x4(
            1, 0, 0, 0,
            0, cos, -sin, 0,
            0, sin, cos, 0,
            0, 0, 0, 1
        );
    }

    // Static function to create a rotation matrix around the Y-axis
    public static Matrix4x4 rotationY(double angle)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Matrix4x4(
            cos, 0, sin, 0,
            0, 1, 0, 0,
            -sin, 0, cos, 0,
            0, 0, 0, 1
        );
    }

    // Static function to create a rotation matrix around the Z-axis
    public static Matrix4x4 rotationZ(double angle)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Matrix4x4(
            cos, -sin, 0, 0,
            sin, cos, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        );
    }
}


class Vector2
{
    double x, y;

    Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
}

class OBJParser
{
    public static Scene parse(String filePath) throws IOException
    {
        Scene scene = new Scene();
        boolean isPartOfFace = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] tokens = line.split(" ");
                switch (tokens[0])
                {
                    case "v":
                        scene.vertices.add(new Vector3(
                                Double.parseDouble(tokens[1]),
                                Double.parseDouble(tokens[2]),
                                Double.parseDouble(tokens[3])
                        ));
                        break;
                    case "vt":
                        scene.uvs.add(new Vector2(
                                Double.parseDouble(tokens[1]),
                                Double.parseDouble(tokens[2])
                        ));
                        break;
                    case "f":
                        // Check if the face spans multiple triangles
                        if (tokens.length > 4)
                        {
                            isPartOfFace = true;
                        }
                        else
                        {
                            isPartOfFace = false;
                        }

                        int[] faceVertices = new int[3];
                        int[] faceUVs = new int[3];
                        for (int i = 0; i < 3; i++)
                        {
                            String[] faceData = tokens[i + 1].split("/");
                            faceVertices[i] = Integer.parseInt(faceData[0]) - 1; // Vertex index
                            faceUVs[i] = Integer.parseInt(faceData[1]) - 1;      // UV index
                        }
                        scene.faces.add(new Face(faceVertices, faceUVs, isPartOfFace));
                        break;
                }
            }
        }
        return scene;
    }
}

class Face
{
    int[] vertices;  // Indices of the vertices
    int[] uvs;       // Indices of the corresponding UV coordinates
    boolean isPartOfFace; // Marks whether this triangle is part of a larger face

    Face(int[] vertices, int[] uvs, boolean isPartOfFace)
    {
        this.vertices = vertices;
        this.uvs = uvs;
        this.isPartOfFace = isPartOfFace;
    }
}

class Camera
{
    Vector3 position;
    Vector3 lookAt;
    double fieldOfView;

    Camera(Vector3 position, Vector3 lookAt, double fieldOfView)
    {
        this.position = position;
        this.lookAt = lookAt;
        this.fieldOfView = fieldOfView;
    }
}

class Vector3
{
    double x, y, z;

    Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vector3 subtract(Vector3 v)
    {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    Vector3 cross(Vector3 v)
    {
        return new Vector3(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x
        );
    }

    double dot(Vector3 v)
    {
        return x * v.x + y * v.y + z * v.z;
    }

    Vector3 normalize()
    {
        double length = Math.sqrt(x * y + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }
}

class Scene
{
    ArrayList<Vector3> vertices = new ArrayList<>();
    ArrayList<Vector2> uvs = new ArrayList<>();
    ArrayList<Face> faces = new ArrayList<>();
}

class RayTracer
{
    public static Intersection intersectRayTriangle(Ray ray, Vector3 v0, Vector3 v1, Vector3 v2)
    {
        final double EPSILON = 0.0000001;
        Vector3 edge1 = v1.subtract(v0);
        Vector3 edge2 = v2.subtract(v0);
        Vector3 h = ray.direction.cross(edge2);
        double a = edge1.dot(h);
        if (a > -EPSILON && a < EPSILON)
            return new Intersection(false, 0, null, null);

        double f = 1.0 / a;
        Vector3 s = ray.origin.subtract(v0);
        double u = f * s.dot(h);
        if (u < 0.0 || u > 1.0)
            return new Intersection(false, 0, null, null);

        Vector3 q = s.cross(edge1);
        double v = f * ray.direction.dot(q);
        if (v < 0.0 || u + v > 1.0)
            return new Intersection(false, 0, null, null);

        double t = f * edge2.dot(q);
        if (t > EPSILON)
        {
            Vector3 intersectionPoint = new Vector3(
                ray.origin.x + ray.direction.x * t,
                ray.origin.y + ray.direction.y * t,
                ray.origin.z + ray.direction.z * t
            );
            Vector3 normal = edge1.cross(edge2).normalize();
            return new Intersection(true, t, intersectionPoint, normal);
        }
        else
            return new Intersection(false, 0, null, null);
    }
}

class Ray
{
    Vector3 origin;
    Vector3 direction;

    Ray(Vector3 origin, Vector3 direction)
    {
        this.origin = origin;
        this.direction = direction;
    }
}

class Intersection
{
    boolean hit;
    double distance;
    Vector3 point;
    Vector3 normal;

    Intersection(boolean hit, double distance, Vector3 point, Vector3 normal)
    {
        this.hit = hit;
        this.distance = distance;
        this.point = point;
        this.normal = normal;
    }
}