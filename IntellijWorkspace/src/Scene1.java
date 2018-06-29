import processing.core.PApplet;

public class Scene1 implements Scene {
    @Override
    public void draw(Renderer R, float xStart, float yStart, float xMin, float yMin, float xMax, float yMax) {
        PApplet p5 = R.getP5();

        float xPos = xStart;
        float yPos = yStart;

        R.raisePen();

        R.moveTo(xPos,yPos);

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                float pX = p5.map(x,0,10,xPos,p5.min(xPos+80,xMax));
                float pY = p5.map(y,0,10,yPos,p5.min(yPos+80,yMax));

                R.ellipse(pX+p5.noise(x), pY+p5.noise(y), 1);
            }
        }
    }
}
