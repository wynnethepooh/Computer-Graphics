/*******************************************************************************
 * File: Polygon.java
 * Author: Wynne Tran
 * Class: CS 445 – Computer Graphics
 * <p>
 * Assignment: program 2
 * Date last modified: 10/25/2015
 * Purpose: Holds information for the polygon.
 ******************************************************************************/
package program2;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * @author wynnetran
 */
public class Polygon {

    private float[] color;
    private List<float[]> vertices;
    private List<Object[]> transformations;
    private float[][] allEdges;
    private List<float[]> globalEdge;
    private List<float[]> activeEdge;
    private float scanLine;
    
    private float midX;
    private float midY;

    public Polygon(float[] rgb, List<float[]> vertices,
                   List<Object[]> transformations) {

        // Set the color of the polygon
        this.color = new float[3];
        for (int ndx = 0; ndx < color.length; ndx++) {
            this.color[ndx] = rgb[ndx];
        }

        // Set vertices
        this.vertices = new ArrayList();
        for (float[] vertex : vertices) {
            this.vertices.add(vertex);
        }
        
        // Find max and min X and max and min Y to find midpoint
        float maxX = this.vertices.get(0)[0];
        float maxY = this.vertices.get(0)[1];
        float minX = this.vertices.get(0)[0];
        float minY = this.vertices.get(0)[1];
        for (float[] vertex : this.vertices) {
            if (vertex[0] > maxX) {
                maxX = vertex[0];
            } if (vertex[0] < minX) {
                minX = vertex[0];
            } if (vertex[1] > maxY) {
                maxY = vertex[1];
            } if (vertex[1] < minY) {
                minY = vertex[1];
            }
        }
        
        midX = minX + ((maxX - minX) / 2);
        midY = minY + ((maxY - minY) / 2);

        // Set transformations
        this.transformations = new ArrayList();
        if (transformations != null) {
            for (Object[] transformation : transformations) {
                this.transformations.add(transformation);
            }
        }

    }

    /**
     * Draws and fills polygon.
     */
    public void draw() {
        
        // Apply transformations
        for (int ndx = transformations.size() - 1; ndx >= 0; ndx--) {
            Object[] transformation = transformations.get(ndx);
            if (transformation[0].equals("r")) {
                
                // Translate back
                glTranslatef(midX, midY, 0);
                
                // Rotate
                glRotatef((float) transformation[1], (float) transformation[2],
                        (float) transformation[3], 1);
                
                // Translate to origin
                glTranslatef(midX - (2 * midX), midY - (2 * midY),0);
                
            } else if (transformation[0].equals("s")) {
                
                // Translate back
                glTranslatef(midX, midY, 0);
                
                // Scale
                glScalef((float) transformation[1],
                        (float) transformation[2], 0);
                
                // Translate to origin
                glTranslatef(midX - (2 * midX), midY - (2 * midY),0);
                
            } else if (transformation[0].equals("t")) {
                glTranslatef((float) transformation[1],
                        (float) transformation[2], 0);
            }
        }

        // Initialize all edges table
        initializeAllEdgesTable();

        // Initialize global edge table
        initializeGlobalEdgeTable();

        // Initialize parity to even (true)
        boolean parity = true;

        // Initialize scanline
        float[] firstEdgeValue = globalEdge.get(0);
        scanLine = firstEdgeValue[0];

        // Initialize active edge table
        initializeActiveEdgeTable();

        float startingEdge = -1;

        while (!activeEdge.isEmpty()) {

            // Go through active edge table and get x values and alternate
            // parities
            for (int ndx = 0; ndx < activeEdge.size(); ndx++) {
                float[] edge = activeEdge.get(ndx);

                if (parity) {
                    startingEdge = edge[1];
                    parity = false;
                } else {
                    drawLine(startingEdge, edge[1]);
                    parity = true;
                }

                // Update x using formula x = x + 1/m
                edge[1] = edge[1] + edge[2];
            }

            // Increment the scanline and remove edges from the active edge
            // table whose max y equals the scanline
            scanLine++;
            for (int ndx = 0; ndx < activeEdge.size(); ndx++) {
                if (activeEdge.get(ndx)[0] == scanLine) {
                    activeEdge.remove(ndx--);
                }
            }

            // Add edges from global edge table whose min y values are equal
            // to the scanline
            for (float[] edge : globalEdge) {
                if (edge[0] == scanLine) {
                    float[] edgeValue = new float[3];
                    System.arraycopy(edge, 1, edgeValue, 0, 3);
                    activeEdge.add(edgeValue);
                }
            }

            // Re-sort the active edge table so that the x's are in
            // increasing order
            Collections.sort(activeEdge, new Comparator<float[]>() {

                @Override
                public int compare(float[] edge1, float[] edge2) {
                    float result = Float.compare(edge1[1], edge2[1]);
                    return (int) result;
                }
            });
        }
        
        glLoadIdentity();
        
    }

    /**
     * Creates all_edges table. Each index contains an edge number with a
     * pointer to an array containing the minimum y-value, maximum y-value, x
     * value of the minimum y-value, and slope.
     */
    private void initializeAllEdgesTable() {
        allEdges = new float[vertices.size()][4];

        for (int ndx = 0; ndx < vertices.size(); ndx++) {
            // Make sure second index doesn't go out of bounds
            int secondIndex = ((ndx + 1) == vertices.size()) ? 0 : ndx + 1;

            float[] vertex0 = vertices.get(ndx);           // first vertex
            float[] vertex1 = vertices.get(secondIndex);   // second vertex
            float x0 = vertex0[0]; // x value of first vertex
            float y0 = vertex0[1]; // y value of first vertex
            float x1 = vertex1[0]; // x value of second vertex
            float y1 = vertex1[1]; // y value of second vertex

            float[] edgeValues = new float[4];

            // Get minimum y-value and x value of minimum y-value
            if (y0 > y1) {
                edgeValues[0] = y1;
                edgeValues[2] = x1;
            } else {
                edgeValues[0] = y0;
                edgeValues[2] = x0;
            }

            edgeValues[1] = Math.max(y0, y1);       // max y value
            edgeValues[3] = (x0 - x1) / (y0 - y1);  // 1/slope

            // Add edge to all_edges table
            allEdges[ndx] = edgeValues;
        }
    }

    /**
     * Initializes the global edge table.
     */
    private void initializeGlobalEdgeTable() {

        this.globalEdge = new ArrayList();
        boolean addedFirstEdge = false;

        // Go through each edge and add it to the global edge table if 1/m != 0
        for (int ndx = 0; ndx < allEdges.length; ndx++) {

            if (allEdges[ndx][3] != Float.POSITIVE_INFINITY
                    && allEdges[ndx][3] != Float.NEGATIVE_INFINITY) {

                if (addedFirstEdge == false) {
                    float[] edge = new float[4];
                    System.arraycopy(allEdges[ndx], 0, edge, 0, 4);
                    globalEdge.add(edge);
                    addedFirstEdge = true;
                } else {
                    boolean addedEdge = false;

                    // Add edge to global edge table so that it's sorted by min
                    // y-value and then x value of min y-value
                    for (int i = 0; i < globalEdge.size(); i++) {

                        if (!addedEdge) {

                            float[] globalEdgeValue = globalEdge.get(i);

                            // If the edge being added has a greater min y than
                            // the current global edge's min y value, continue
                            if (allEdges[ndx][0] > globalEdgeValue[0]) {
                                continue;
                            } else {

                                if ((allEdges[ndx][0] == globalEdgeValue[0] &&
                                        allEdges[ndx][2] <= globalEdgeValue[2])
                                        || allEdges[ndx][0]
                                        < globalEdgeValue[0]) {

                                    // If the edge being added has a lesser min
                                    // y and x value than the current global
                                    // edge, add it to the global edges table
                                    float[] edge = new float[4];
                                    System.arraycopy(allEdges[ndx], 0,
                                            edge, 0, 4);
                                    globalEdge.add(i++, edge);
                                    addedEdge = true;
                                }
                            }
                        }
                    }

                    if (!addedEdge) {
                        float[] edge = new float[4];
                        System.arraycopy(allEdges[ndx], 0, edge, 0, 4);
                        globalEdge.add(edge);
                        addedEdge = true;
                    }
                }
            }
        }
    }

    /**
     * Initializes the active edge table.
     */
    private void initializeActiveEdgeTable() {
        activeEdge = new ArrayList();

        int ndx = 0;
        float[] edge = globalEdge.get(0);

        while (edge[0] == scanLine) {
            float[] newEdge = new float[3];
            System.arraycopy(edge, 1, newEdge, 0, 3);

            // Move edge from global edge table to active edge table
            activeEdge.add(newEdge);
            globalEdge.remove(0);

            // Get new index 0 of global edge table
            edge = globalEdge.get(0);
        }
    }

    /**
     * Gets edge values in array with minimum y-value, maximum y-value, x value
     * of minimum y-value, and 1/m.
     *
     * @param map map of size one mapping edge index to edge values
     * @return edge values
     */
    private float[] getEdgeValues(Map<Integer, float[]> map) {
        for (int index : map.keySet()) {
            return map.get(index);
        }
        return null;
    }

    /**
     * Draws line.
     * @param startingX starting endpoint of line
     * @param endingX other endpoint of line
     */
    private void drawLine(final float startingX, final float endingX) {
        // Set color and size of points
        glColor3f(color[0], color[1], color[2]);
        glPointSize(2);

        float x = startingX;

        // Begin drawing
        glBegin(GL_POINTS);
        
        // While we haven't drawn the end point
        while (x <= endingX) {
            glVertex2f(x++, scanLine);
        }

        glEnd();
    }
    
    /**
     * Sets color of polygon.
     * @param color color to set the polygon
     */
    public void setColor(float[] color) {
        System.arraycopy(color, 0, this.color, 0, 3);
    }

}
