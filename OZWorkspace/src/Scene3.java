public class Scene3 implements Scene {
    @Override
    public void draw(Renderer R, float xMin, float yMin, float xMax, float yMax) {
        float xPos = (xMax-xMin)/2.0f;
        float yPos = (yMax-yMin)/2.0f;

        R.rect(xPos,yPos,100,100);
    }
}
