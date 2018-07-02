import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.append;
import static processing.core.PApplet.println;
import static processing.core.PApplet.radians;
import static processing.core.PConstants.CLOSE;

public class Renderer {
    private PApplet p5;
    private boolean isInBeginShape = false;
    Shape shape;

    public Renderer(PApplet p5) {
        this.p5 = p5;
    }

    public void beginShape(ShapeType type) {
        isInBeginShape = true;
        shape = new Shape(type);
    }

    public void endShape(){
        endShape(ShapeOpenClose.OPEN);
    }

    public void endShape(ShapeOpenClose closeType) {
        assert(shape != null);
        shape.setCloseType(closeType);
        isInBeginShape = false;

        switch (shape.getType()) {
            case POINTS:
                renderShapeAsPoints();
                break;
            case LINES:
                renderShapeAsLines();
                break;
            case LINE_STRIP:
                renderShapeAsLineStrip();
                break;
        }
    }

    public void vertex(float x, float y) {
        assert (isInBeginShape);
        assert (shape != null);
        shape.addVertex(x, y);
    }

    private void renderShapeAsLines() {
        raisePen();
        ArrayList<Vertex> vertices = shape.getVertices();
        if (vertices.size() < 2) {
            p5.println("shape has less than at least two vertices");
            return;
        }

        for (int i = 0; i < vertices.size() - 1; i+=2) {
            PVector A = vertices.get(i).getPosition();
            PVector B = vertices.get(i + 1).getPosition();

            moveTo(A.x,A.y);
            lowerPen();
            moveTo(B.x,B.y);
            raisePen();
        }
    }

    private void renderShapeAsLineStrip() {
        raisePen();
        ArrayList<Vertex> vertices = shape.getVertices();
        if (vertices.size() < 2) {
            p5.println("shape has less than at least two vertices");
            return;
        }

        PVector firstPosition = vertices.get(0).getPosition();
        moveTo(firstPosition.x,firstPosition.y);
        lowerPen();
        for (int i = 1; i < vertices.size(); i++) {
            PVector p = vertices.get(i).getPosition();
            moveTo(p.x,p.y);
        }
        if(shape.getCloseType() == ShapeOpenClose.CLOSE){
            moveTo(firstPosition.x,firstPosition.y);
        }

        raisePen();
    }

    private void renderShapeAsPoints() {
        raisePen();
        ArrayList<Vertex> vertices = shape.getVertices();

        for (Vertex v : vertices) {
            PVector position = v.getPosition();
            moveTo(position.x, position.y);
            lowerPen();
            raisePen();
        }
    }

    public void raisePen() {
        moveTo(-30, 0); //Command 30 (raise pen)
    }

    public void moveTo(float x, float y) {
        MainClass.ToDoList = (PVector[]) append(MainClass.ToDoList, new PVector(x, y));
    }

    public PApplet getP5() {
        return p5;
    }

    public void lowerPen() {
        moveTo(-31, 0);  //Command 31 (lower pen)
    }

    public void point(float x, float y) {
        beginShape(ShapeType.POINTS);
        vertex(x,y);
        endShape();
    }

    public void ellipse(float x, float y, float radius) {
        beginShape(ShapeType.LINE_STRIP);
        float xPos = x;
        float yPos = y;

        int deltaAngle = 20;
        int N = 360 / deltaAngle;
        for (int i = 1; i < N; i++) {
            float r = radians(i * deltaAngle);
            xPos = xPos + (radius * p5.cos(r));
            yPos = yPos + (radius * p5.sin(r));

            if ((xPos < MainClass.MousePaperLeft) || (xPos > MainClass.MousePaperRight))
                break;
            if ((yPos < MainClass.MousePaperTop) || (yPos > MainClass.MousePaperBottom))
                break;

            // Command Code: Move to (X,Y)
            vertex(xPos, yPos);
        }

        endShape(ShapeOpenClose.CLOSE);
    }

    public void rect(float x,float y, float width, float height){
        beginShape(ShapeType.LINE_STRIP);
        vertex(x,y);
        vertex(x,y+height);
        vertex(x+width,y+height);
        vertex(x+width,y);
        endShape(ShapeOpenClose.CLOSE);
    }
}