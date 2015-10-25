/*******************************************************************************
* File: Program2.java
* Author: Wynne Tran
* Class: CS 445 – Computer Graphics
*
* Assignment: program 2
* Date last modified: 10/22/2015
* Purpose: Draws a window in the center of the screen and reads coordinates to 
* create filled polygons from a coordinates.txt file. 
*******************************************************************************/
package program2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author wynnetran
 */
public class Program2 {
    
    private final String COORDINATES = "coordinates.txt";
    private List<Polygon> polygons;
    
    public Program2() {}
    
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
        Display.setTitle("CS 445 Program 2");
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
            
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                                
                // Draw polygons
                for (Polygon polygon : this.polygons) {
                    polygon.draw();
                }
                
                Display.update();
                Display.sync(60);
            } catch (Exception e) {
                
            }
        }
        
        Display.destroy();
    }
    
    /**
     * Parses coordinates file and gets list of polygons with transformations.
     * @return list of polygons with transformations
     */
    private void parseFile() {
        BufferedReader br = null;
        FileReader fr = null;
        String line;
        this.polygons = new ArrayList();

        Polygon polygon = null;
        float[] color = null;
        List<float[]> vertices = null;
        List<Object[]> transformations = null;

        try {
            fr = new FileReader(COORDINATES);
            br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");

                // Start new polygon
                if (tokens[0].equalsIgnoreCase("P")) {

                    if (color != null && vertices != null) {

                        // Create polygon with information from file
                        polygon = new Polygon(color, vertices, transformations);

                        // Reset vertices and transformations
                        vertices = null;
                        transformations = null;

                    }

                    color = new float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    };

                    // If a new polygon is being started, add the existing 
                    // polygon information to the list of polygons
                    if (polygon != null) {
                        polygons.add(polygon);
                        polygon = null;
                    }

                } else if (tokens[0].equals("T")) {

                    // Start list of transformations
                    transformations = new ArrayList();

                } else if (tokens[0].equals("r")) {

                    // Get transformation information and add to list
                    Object[] transformation = new Object[] {
                            tokens[0],
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    };

                    transformations.add(transformation);

                } else if (tokens[0].equals("s")) {

                    // Get transformation information and add to list
                    Object[] transformation = new Object[]{
                            tokens[0],
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]),
                            Float.parseFloat(tokens[4])
                    };

                    transformations.add(transformation);

                } else if (tokens[0].equals("t")) {

                    // Get transformation information and add to list
                    Object[] transformation = new Object[]{
                            tokens[0],
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    };

                    transformations.add(transformation);

                } else {

                    if (vertices == null) {
                        vertices = new ArrayList();
                    }

                    // Get vertex from file and add to list of vertices
                    float[] vertex = new float[] {
                            Float.parseFloat(tokens[0]),
                            Float.parseFloat(tokens[1])
                    };

                    vertices.add(vertex);
                }
            }

            if (color != null && vertices != null) {

                // Create polygon with information from file
                polygon = new Polygon(color, vertices, transformations);
                polygons.add(polygon);
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
    }
    
    /**
     * Starts program and draws shapes from coordinates file.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Program2 program = new Program2();
        program.parseFile();
        program.start();
    }
}
