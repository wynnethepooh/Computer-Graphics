/*******************************************************************************
* File: Program1.java
* Author: Wynne Tran
* Class: CS 445 – Computer Graphics
*
* Assignment: program 1
* Date last modified: 10/06/2015
* Purpose: Draws a window in the center of the screen and reads coordinates to 
* create shapes from a coordinates.txt file. Lines are drawn in red, circles are
* drawn in blue, and ellipses are drawn in green. Pressing (and holding) the "l"
* key changes the color of the lines, pressing the "c" key changes the color of
* the circles, and pressing the "e" key changes the color of the ellipses.
*******************************************************************************/
package program1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author wynnetran
 */
public class Program1 {
    
    private final String COORDINATES = "coordinates.txt";
    private List<String[]> coordinatesList;
    private float[] lineColor;
    private float[] circleColor;
    private float[] ellipseColor;
    private Random random;
    
    public Program1() {
        lineColor = new float[] {1.0f, 0.0f, 0.0f};
        circleColor = new float[] {0.0f, 0.0f, 1.0f};
        ellipseColor = new float[] {0.0f, 1.0f, 0.0f};
        random = new Random();
    }
    
    /**
     * Creates window and draws shapes from coordinates file.
     */
    public void start() {
        try {
            createWindow();
            initGL();
            render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates window for program.
     * @throws Exception exception when creating window
     */
    public void createWindow() throws Exception {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("CS 445 Program 1");
        Display.create();
    }
    
    /**
     * Sets up window.
     */
    private void initGL() {
        // Specify background color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        // Load our camera using projection to view our scene
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        
        // Setup our orthographic matrix with a size of 640 by 480 with a
        // clipping distance between 1 and -1
        glOrtho(0, 640, 0, 480, 1, -1);
        
        // Set up our scene to Model view, and provide some rendering hints
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
    
    /**
     * Renders and creates shapes specified in coordinates file.
     */
    private void render() {
        // Called for every frame
        while (!Display.isCloseRequested()
                && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            
            // If user presses "l" key, line color changes
            if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                lineColor = new float[] {
                    random.nextFloat(),
                    random.nextFloat(),
                    random.nextFloat()
                };
            }
            
            // If user presses "c" key, circle color changes
            if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                circleColor = new float[] {
                    random.nextFloat(),
                    random.nextFloat(),
                    random.nextFloat()
                };
            }
            
            // If user presses "e" key, ellipse color changes
            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                ellipseColor = new float[] {
                    random.nextFloat(),
                    random.nextFloat(),
                    random.nextFloat()
                };
            }
            
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                
                this.coordinatesList = readFile();
                
                for (String[] coordinate : coordinatesList) {
                    if (coordinate[0].equals("l")) {    // line
                        
                        // Get the start and end points
                        String[] startPoint = coordinate[1].split(",");
                        String[] endPoint = coordinate[2].split(",");
                        
                        if (startPoint.length == 2 && endPoint.length == 2) {
                            float[] startCoordinates = new float[] {
                                Float.parseFloat(startPoint[0]),
                                Float.parseFloat(startPoint[1])
                            };
                            float[] endCoordinates = new float[] {
                                Float.parseFloat(endPoint[0]),
                                Float.parseFloat(endPoint[1])
                            };
                            
                            // Draw the line
                            drawLine(startCoordinates, endCoordinates);
                            
                        } else {
                            System.err.println("Incorrect coordinate format");
                        }
                        
                    } else if (coordinate[0].equals("c")) { // circle
                        
                        // Get center and radius
                        String[] center = coordinate[1].split(",");
                        float[] centerPoint = new float[] {
                            Float.parseFloat(center[0]),
                            Float.parseFloat(center[1])
                        };
                        float radius = Float.parseFloat(coordinate[2]);
                        
                        drawCircle(centerPoint, radius);
                        
                    } else if (coordinate[0].equals("e")) { // ellipse
                        
                        // Get center and radii
                        String[] center = coordinate[1].split(",");
                        float[] centerPoint = new float[] {
                            Float.parseFloat(center[0]),
                            Float.parseFloat(center[1])
                        };
                        String[] radii = coordinate[2].split(",");
                        float[] radiiLengths = new float[] {
                            Float.parseFloat(radii[0]),
                            Float.parseFloat(radii[1])
                        };
                        
                        drawEllipse(centerPoint, radiiLengths);
                    }
                }
                
                Display.update();
                Display.sync(60);
            } catch (Exception e) {
                
            }
        }
        
        Display.destroy();
    }
    
    /**
     * Draws line.
     * @param startPoint starting endpoint of line
     * @param endPoint other endpoint of line
     */
    private void drawLine(float[] startPoint, float[] endPoint) {
        // If start point's x coordinate is greater than end point's x
        // coordinate, swap them
        if (endPoint[0] < startPoint[0]) {
            float[] temp = startPoint;
            startPoint = endPoint;
            endPoint = temp;
        }
        
        float x = startPoint[0]; // current x value
        float y = startPoint[1]; // current y value
        float dx = endPoint[0] - startPoint[0]; // change in x
        float dy = endPoint[1] - startPoint[1]; // change in y
        float d = (2 * dy) - dx; // distance to midpoint
        float incrementRight = 2 * dy; // how much to move right
        float incrementUpRight = 2 * (dy - dx); // how much to move up and right
        
        // Set color and size of points
        glColor3f(lineColor[0], lineColor[1], lineColor[2]);
        glPointSize(1);
        
        // Draw first point
        glBegin(GL_POINTS);
        glVertex2f(x, y);
        
        // While we haven't drawn the end point
        while (x < endPoint[0]) {
            if (d > 0) {
                d += incrementUpRight;
                x++;
                y++;
            } else {
                d += incrementRight;
                x++;
                if (dy < 0) {
                    y--;
                }
            }
            
            glVertex2f(x, y);
        }
        
        glEnd();
    }
    
    /**
     * Draws circle.
     * @param center center point of circle
     * @param radius radius of circle
     */
    private void drawCircle(float[] center, float radius) {
        // Set color and size of points
        glColor3f(circleColor[0], circleColor[1], circleColor[2]);
        glPointSize(1);
        
        // Begin drawing
        glBegin(GL_POINTS);
        
        for (double degrees = 0; degrees < 360; degrees++) {
            float x;
            float y;
            double theta = Math.toRadians(degrees);
            
            x = radius * (float) Math.cos(theta);
            y = radius * (float) Math.sin(theta);
            
            // Draw point using center offset by calculated x and y values
            glVertex2f(center[0] + x, center[1] + y);
        }
        
        glEnd();
    }
    
    /**
     * Draws ellipse.
     * @param center center of ellipse
     * @param radii radii of ellipse
     */
    private void drawEllipse(float[] center, float[] radii) {
        // Set color and size of points
        glColor3f(ellipseColor[0], ellipseColor[1], ellipseColor[2]);
        glPointSize(1);
        
        // Begin drawing
        glBegin(GL_POINTS);
        
        for (double degrees = 0; degrees < 360; degrees++) {
            float x;
            float y;
            double theta = Math.toRadians(degrees);
            
            x = radii[0] * (float) Math.cos(theta);
            y = radii[1] * (float) Math.sin(theta);
            
            // Draw point using center offset by calculated x and y values
            glVertex2f(center[0] + x, center[1] + y);
        }
        
        glEnd();
    }
    
    /**
     * Reads coordinates file and gets list of shapes with their coordinates.
     * @return list of shapes with their coordinates
     */
    private List<String[]> readFile() {
        BufferedReader br = null;
        FileReader fr = null;
        String line;
        List<String[]> coordinates = new ArrayList();
        
        try {
            fr = new FileReader(COORDINATES);
            br = new BufferedReader(fr);
            
            while ((line = br.readLine()) != null) {
                String[] coordinateSet = line.split(" ");
                coordinates.add(coordinateSet);
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        
        return coordinates;
    }
    
    /**
     * Starts program and draws shapes from coordinates file.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Program1 program = new Program1();
        program.start();
    }
}
