import processing.core.PApplet;
import processing.core.PVector;

import static processing.core.PApplet.append;
import static processing.core.PApplet.radians;

public class Renderer {

    private PApplet p5;

    public Renderer(PApplet p5) {
        this.p5 = p5;
    }



    public void raisePen(){
        moveTo(-30, 0); //Command 30 (raise pen)
    }

    public void moveTo(float x, float y){
        MainClass.ToDoList = (PVector[]) append(MainClass.ToDoList, new PVector(x,y));
    }

    public PApplet getP5() {
        return p5;
    }

    public void lowerPen(){
        moveTo(-31, 0);  //Command 31 (lower pen)
    }

    public void point(float x, float y) {
        raisePen();
        moveTo(x, y);
        lowerPen();
        raisePen();
    }

    public void ellipse(float x, float y, float radius) {
        raisePen(); //Command 30 (raise pen)
        moveTo(x, y);

        lowerPen();
        float xPos = x;
        float yPos = y;

        int deltaAngle = 20;
        int N = 360/deltaAngle;
        for (int i = 1; i < N; i++) {
            float r = radians(i*deltaAngle);
            xPos = xPos + (radius * p5.cos(r));
            yPos = yPos + (radius * p5.sin(r));

            if ((xPos < MainClass.MousePaperLeft) || (xPos > MainClass.MousePaperRight))
                break;
            if ((yPos < MainClass.MousePaperTop) || (yPos >MainClass. MousePaperBottom))
                break;

            // Command Code: Move to (X,Y)
            moveTo(xPos, yPos);
        }

        raisePen();
    }
}