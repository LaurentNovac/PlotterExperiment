import java.util.ArrayList;

public class Shape {

    public Shape(ShapeType type) {
        this.type = type;
        vertices = new ArrayList<Vertex>();
    }

    public ShapeType getType() {
        return type;
    }

    void addVertex(float x, float y){
        vertices.add(new Vertex(x,y));
    }

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public ShapeOpenClose getCloseType() {
        return closeType;
    }

    public void setCloseType(ShapeOpenClose closeType) {
        this.closeType = closeType;
    }

    private ArrayList<Vertex> vertices;
    private ShapeType type = ShapeType.POINTS;
    private ShapeOpenClose closeType = ShapeOpenClose.CLOSE;
}
