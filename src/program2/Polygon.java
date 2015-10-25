/*******************************************************************************
 * File: Polygon.java
 * Author: Wynne Tran
 * Class: CS 445 – Computer Graphics
 * <p>
 * Assignment: program 2
 * Date last modified: 10/22/2015
 * Purpose: Holds information for the polygon.
 ******************************************************************************/
package program2;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glVertex2f;

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

        // Set transformations
        this.transformations = new ArrayList();
        if (transformations != null) {
            for (Object[] transformation : transformations) {
                this.transformations.add(transformation);
            }
        }

        draw();
    }

    /**
     * Draws and fills polygon.
     */
    public void draw() {

        // Initialize all edges table
        initializeAllEdgesTable();

        // Initialize global edge table
        initializeGlobalEdgeTable();

        // Initialize parity to even (true)
        boolean parity = true;

        // Initialize scanline
        float[] firstEdgeValue = globalEdge.get(0);
        scanLine = firstEdgeValue[1];

        // Initialize active edge table
        initializeActiveEdgeTable();

        while (!activeEdge.isEmpty()) {

        }

    }

    /**
     * Creates all_edges table. Each index contains an edge number with a pointer
     * to an array containing the minimum y-value, maximum y-value, x value of
     * the minimum y-value, and slope.
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
                    float[] edge = new float[5];
                    edge[0] = ndx;
                    System.arraycopy(allEdges[ndx], 0, edge, 1, 4);
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
                            if (allEdges[ndx][0] > globalEdgeValue[1]) {
                                continue;
                            } else {

                                if ((allEdges[ndx][0] == globalEdgeValue[1] &&
                                        allEdges[ndx][2] <= globalEdgeValue[3])
                                        || allEdges[ndx][0]
                                        < globalEdgeValue[1]) {

                                    // If the edge being added has a lesser min
                                    // y and x value than the current global
                                    // edge, add it to the global edges table
                                    float[] edge = new float[5];
                                    edge[0] = ndx;
                                    System.arraycopy(allEdges[ndx], 0, edge, 1, 4);
                                    globalEdge.add(i++, edge);
                                    addedEdge = true;
                                }
                            }
                        }
                    }

                    if (!addedEdge) {
                        float[] edge = new float[5];
                        edge[0] = ndx;
                        System.arraycopy(allEdges[ndx], 0, edge, 1, 4);
                        globalEdge.add(edge);
                        addedEdge = true;
                    }
                }
            }
        }

        // Go through global edge table and reset the indices
        for (int ndx = 0; ndx < globalEdge.size(); ndx++) {
            float[] edge = globalEdge.get(ndx);
            edge[0] = ndx;
        }
    }

    /**
     * Initializes the active edge table.
     */
    private void initializeActiveEdgeTable() {
        activeEdge = new ArrayList();

        int ndx = 0;
        float[] edge = globalEdge.get(0);

        while (edge[1] == scanLine) {
            float[] newEdge = new float[4];
            newEdge[0] = edge[0];
            System.arraycopy(edge, 2, newEdge, 1, 3);

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
        glPointSize(1);

        float x = startingX;

        // Draw first point
        glBegin(GL_POINTS);
        glVertex2f(x, scanLine);

        // While we haven't drawn the end point
        while (x <= endingX) {
            glVertex2f(x++, scanLine);
        }

        glEnd();
    }

}
