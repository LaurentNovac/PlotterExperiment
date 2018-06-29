import processing.core.*;

import java.util.ArrayList;
// A simple text button class, that allows you to specify a text label and x&y
// coordinates.

public class SimpleButton {
    private PApplet p5;
    public String label;
    private float posX;
    private float posY;
    private float buttonWidth;
    private float buttonHeight;

    // These are optional attributes
    private PFont font;
    private int fontSize;
    public int displayColor;
    public int highlightColor;


    SimpleButton(PApplet p5, String label, int posX, int posY) {
        this.p5 = p5;
        initButton(label, posX, posY, (PFont) null, 0, p5.color(255, 255, 255, 128), p5.color(255));
    }

    SimpleButton(PApplet p5, String label, int posX, int posY,
                 PFont font, int fontSize,
                 int displayColor, int highlightColor) {
        this.p5 = p5;
        initButton(label, posX, posY, font, fontSize, displayColor, highlightColor);
    }

    void initButton(String label, int posX, int posY,
                    PFont font, int fontSize,
                    int displayColor, int highlightColor) {
        if (MainClass.buttonList == null) {
            // If this is the first button, we need to create a list to store them in.
            MainClass.buttonList = new ArrayList();
        }

        this.posX = posX;
        this.posY = posY;
        this.font = font;
        this.fontSize = fontSize;
        this.displayColor = displayColor;
        this.highlightColor = highlightColor;
        updateLabel(label);

        MainClass.buttonList.add(this);
    }

    void draw(PGraphics b) {

        // Determine
        if (isSelected()) {
            b.fill(highlightColor);
        } else {
            b.fill(displayColor);
        }

        if (font != null) {
            b.textFont(font, fontSize);
        }

        b.text(label, posX, posY);
    }

    void delete() {
        // Delete this button
        // TODO: Find self in button list and remove.
    }

    void updateLabel(String label) {
        this.label = label;

        if (font != null) {
            p5.textFont(font, fontSize);
        }

        buttonWidth = p5.textWidth(label);
        buttonHeight = p5.textAscent();
    }

    boolean isSelected() {
        return (overRect(posX, posY - buttonHeight, buttonWidth, buttonHeight));
    }

    boolean overRect(float x, float y, float width, float height) {
        if (p5.mouseX >= x && p5.mouseX <= x + width && p5.mouseY >= y && p5.mouseY <= y + height)
            return true;
        else
            return false;
    }
}