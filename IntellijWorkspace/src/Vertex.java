import processing.core.PVector;

public class Vertex {
    public Vertex(float x, float y) {
        this.position = new PVector(x,y);
    }

    public PVector getPosition() {
        return position;
    }

    private PVector position;
}
