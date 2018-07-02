import processing.core.PApplet;

public class Scene2 implements Scene {
    @Override
    public void draw(Renderer R, float xMin, float yMin, float xMax, float yMax) {
        float xPos = (xMax-xMin)/2.0f;
        float yPos = (yMax-yMin)/2.0f;

        R.raisePen();

        R.beginShape(ShapeType.LINE_STRIP);
        for (int x = 0; x < 100; x++) {
            R.vertex(xPos+x,yPos);
        }
        R.endShape();
    }
}
