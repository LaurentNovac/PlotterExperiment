import processing.core.PApplet;
import processing.core.*;

import processing.serial.*;
import de.looksgood.ani.*;

import java.util.ArrayList;

/*
  OZ

  Based on AxiGen

  Generative art example with AxiDraw
  https://github.com/evil-mad/AxiDraw-Processing


   Based on RoboPaint RT:
   https://github.com/evil-mad/robopaint-rt

 */


public class MainClass extends PApplet {


    // User Settings:
    float MotorSpeed = 4000.0f;  // Steps per second, 1500 default

    int ServoUpPct = 60;    // Brush UP position, %  (higher number lifts higher).
    int ServoPaintPct = 40;    // Brush DOWN position, %  (higher number lifts higher).

    boolean reverseMotorX = false;
    boolean reverseMotorY = false;

    int delayAfterRaisingBrush = 300; //ms
    int delayAfterLoweringBrush = 300; //ms

    //boolean debugMode = true;
    boolean debugMode = false;

    boolean PaperSizeA4 = false; // true for A4. false for US letter.

    Renderer R;
    ArrayList<Scene> scenes;
// Offscreen buffer images for holding drawn elements, makes redrawing MUCH faster

    PGraphics offScreen;

    PImage imgBackground;   // Stores background data image only.
    PImage imgMain;         // Primary drawing canvas
    PImage imgLocator;      // Cursor crosshairs
    PImage imgButtons;      // Text buttons
    PImage imgHighlight;
    String BackgroundImageName = "background.png";
    String HelpImageName = "help.png";

    boolean segmentQueued = false;
    PVector queuePt1 = new PVector(-1, -1);
    PVector queuePt2 = new PVector(-1, -1);

    float MotorStepsPerPixel = 32.1f;// Good for 1/16 steps-- standard behavior.
    float PixelsPerInch = 63.3f;

// Hardware resolution: 1016 steps per inch @ 50% max resolution
// Horizontal extent in this window frame is 740 px.
// 2032 steps per inch * (11.69 inches (i.e., A4 length)) per 740 px gives 16.05 motor steps per pixel.
// Vertical travel for 8.5 inches should be  (8.5 inches * 2032 steps/inch) / (32.1 steps/px) = 538 px.
// PixelsPerInch is given by (2032 steps/inch) / (32.1 steps/px) = 63.3 pixels per inch


// Positions of screen items

    public static int MousePaperLeft = 30;
    public static int MousePaperRight = 770;
    public static int MousePaperTop = 62;
    public static int MousePaperBottom = 600;

    int yBrushRestPositionPixels = 6;


    int ServoUp;    // Brush UP position, native units
    int ServoPaint;    // Brush DOWN position, native units.

    int MotorMinX;
    int MotorMinY;
    int MotorMaxX;
    int MotorMaxY;

    int Black = color(25, 25, 25);  // BLACK
    int PenColor = Black;

    boolean firstPath;
    boolean doSerialConnect = true;
    boolean SerialOnline;
    Serial myPort;  // Create object from Serial class
    int val;        // Data received from the serial port

    boolean BrushDown;
    boolean BrushDownAtPause;
    boolean DrawingPath = false;

    int xLocAtPause;
    int yLocAtPause;

    int MotorX;  // Position of X motor
    int MotorY;  // Position of Y motor
    int MotorLocatorX;  // Position of motor locator
    int MotorLocatorY;
    PVector lastPosition; // Record last encoded position for drawing


    boolean forceRedraw;
    boolean shiftKeyDown;
    boolean keyup = false;
    boolean keyright = false;
    boolean keyleft = false;
    boolean keydown = false;
    boolean hKeyDown = false;
    int lastButtonUpdateX = 0;
    int lastButtonUpdateY = 0;

    boolean lastBrushDown_DrawingPath;
    int lastX_DrawingPath;
    int lastY_DrawingPath;


    int NextMoveTime;          //Time we are allowed to begin the next movement (i.e., when the current move will be complete).
    int SubsequentWaitTime = -1;    //How long the following movement will take.
    int UIMessageExpire;
    int raiseBrushStatus;
    int lowerBrushStatus;
    int moveStatus;
    int MoveDestX;
    int MoveDestY;
    int PaintDest;

    boolean Paused;

    public static PVector[] ToDoList;  // Queue future events in an array; Coordinate/command
// X-coordinate, Y Coordinate.
// If X-coordinate is negative, that is a non-move command.


    int indexDone;    // Index in to-do list of last action performed
    int indexDrawn;   // Index in to-do list of last to-do element drawn to screen


    // Active buttons
    PFont font_ML16;
    PFont font_CB; // Command button font


    int TextColor = 75;
    int LabelColor = 150;
    int TextHighLight = Black;
    int DefocusColor = 175;

    SimpleButton pauseButton;
    SimpleButton brushUpButton;
    SimpleButton brushDownButton;
    SimpleButton parkButton;
    SimpleButton motorOffButton;
    SimpleButton motorZeroButton;
    SimpleButton clearButton;
    SimpleButton replayButton;
    SimpleButton urlButton;
    SimpleButton quitButton;


    SimpleButton brushLabel;
    SimpleButton motorLabel;
    SimpleButton UIMessage;

    // These keep track of all the buttons that there are
    static ArrayList buttonList;  // List of all buttons

    // Function to draw all of the buttons at once
    static void DrawButtons(PGraphics buffer) {
        if (buttonList == null) {
            return;
        }

        for (int i = buttonList.size() - 1; i >= 0; i--) {
            SimpleButton button = (SimpleButton) buttonList.get(i);
            button.draw(buffer);
        }
    }

    // Return the [x,y] of the motor position in pixels
    int[] getMotorPixelPos() {
        int[] out = {
                (int) ((float) (MotorX) / MotorStepsPerPixel) + MousePaperLeft,
                (int) ((float) (MotorY) / MotorStepsPerPixel) + MousePaperTop + yBrushRestPositionPixels
        };
        return out;
    }


    // Get float distance between two non-encoded (x,y) positions.
    float getDistance(int x1, int y1, int x2, int y2) {
        int xdiff = abs(x2 - x1);
        int ydiff = abs(y2 - y1);
        return sqrt(pow(xdiff, 2) + pow(ydiff, 2));
    }

    void scanSerial() {

        // Serial port search string:
        int PortCount = 0;
        String portName;
        String str1, str2;
        int j;


        int OpenPortList[];
        OpenPortList = new int[0];


        SerialOnline = false;
        boolean serialErr = false;


        try {
            PortCount = Serial.list().length;
        } catch (Exception e) {
            e.printStackTrace();
            serialErr = true;
        }


        if (serialErr == false) {

            println("\nI found " + PortCount + " serial ports, which are:");
            println(Serial.list());


            String os = System.getProperty("os.name").toLowerCase();
            boolean isMacOs = os.startsWith("mac os x");
            boolean isWin = os.startsWith("win");


            if (isMacOs) {
                str1 = "/dev/tty.usbmodem";       // Can change to be the name of the port you want, e.g., COM5.
                // The default value is "/dev/cu.usbmodem"; which works on Macs.

                str1 = str1.substring(0, 14);

                j = 0;
                while (j < PortCount) {
                    str2 = Serial.list()[j].substring(0, 14);
                    if (str1.equals(str2) == true)
                        OpenPortList = append(OpenPortList, j);

                    j++;
                }
            } else if (isWin) {
                // All available ports will be listed.

                j = 0;
                while (j < PortCount) {
                    OpenPortList = append(OpenPortList, j);
                    j++;
                }
            } else {
                // Assume linux

                str1 = "/dev/ttyACM";
                str1 = str1.substring(0, 11);

                j = 0;
                while (j < PortCount) {
                    str2 = Serial.list()[j].substring(0, 11);
                    if (str1.equals(str2) == true)
                        OpenPortList = append(OpenPortList, j);
                    j++;
                }
            }


            boolean portErr;

            j = 0;
            while (j < OpenPortList.length) {

                portErr = false;
                portName = Serial.list()[OpenPortList[j]];

                try {
                    myPort = new Serial(this, portName, 38400);
                } catch (Exception e) {
                    SerialOnline = false;
                    portErr = true;
                    println("Serial port " + portName + " could not be activated.");
                }

                if (portErr == false) {
                    myPort.buffer(1);
                    myPort.clear();
                    println("Serial port " + portName + " found and activated.");

                    String inBuffer = "";

                    myPort.write("v\r");  //Request version number
                    delay(50);  // Delay for EBB to respond!

                    while (myPort.available() > 0) {
                        inBuffer = myPort.readString();
                        if (inBuffer != null) {
                            println("Version Number: " + inBuffer);
                        }
                    }

                    str1 = "EBB";
                    if (inBuffer.length() > 2) {
                        str2 = inBuffer.substring(0, 3);
                        if (str1.equals(str2) == true) {
                            // EBB Identified!
                            SerialOnline = true;    // confirm that this port is good
                            j = OpenPortList.length; // break out of loop

                            println("Serial port " + portName + " confirmed to have EBB.");
                        } else {
                            myPort.clear();
                            myPort.stop();
                            println("Serial port " + portName + ": No EBB detected.");
                        }
                    }
                }
                j++;
            }
        }
    }


    // Only need to redraw if hovering or changing state
    void redrawButtons() {


        offScreen.beginDraw();
        offScreen.background(0, 0);

        DrawButtons(offScreen);

        offScreen.endDraw();

        imgButtons = offScreen.get(0, 0, offScreen.width, offScreen.height);
    }


    // Only need to redraw if hovering or change select on specific items
    void redrawHighlight() {
        offScreen.beginDraw();
        offScreen.background(0, 0);

        offScreen.endDraw();
        imgHighlight = offScreen.get(0, 0, offScreen.width, offScreen.height);

        // TODO: Remove this section?
    }


    // Draw the locator crosshair to the offscreen buffer and fill imgLocator with it
// Only need to redraw this when it changes color
    void redrawLocator() {
        offScreen.beginDraw();
        offScreen.background(0, 0);

        offScreen.stroke(0, 0, 0, 128);
        offScreen.strokeWeight(2);
        int x0 = 10;
        int y0 = 10;

        if (BrushDown)
            offScreen.fill(PenColor);
        else
            offScreen.noFill();

        offScreen.ellipse(x0, y0, 10, 10);

        offScreen.line(x0 + 5, y0, x0 + 10, y0);
        offScreen.line(x0 - 5, y0, x0 - 10, y0);
        offScreen.line(x0, y0 + 5, x0, y0 + 10);
        offScreen.line(x0, y0 - 5, x0, y0 - 10);
        offScreen.endDraw();

        imgLocator = offScreen.get(0, 0, 25, 25);
    }

    void zero() {
        // Mark current location as (0,0) in motor coordinates.
        // Manually move the motor carriage to the left-rear (upper left) corner before executing this command.

        MotorX = 0;
        MotorY = 0;

        moveStatus = -1;
        MoveDestX = -1;
        MoveDestY = -1;

        // Calculate and animate position location cursor
        int[] pos = getMotorPixelPos();
        float sec = .25f;

        Ani.to(this, sec, "MotorLocatorX", pos[0]);
        Ani.to(this, sec, "MotorLocatorY", pos[1]);


        //  if (debugMode) println("Motor X: " + MotorX + "  Motor Y: " + MotorY);
    }

    void clearall() {  // ***** CLEAR ALL *****

        ToDoList = new PVector[0];

        ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (Raise pen)
        ToDoList = (PVector[]) append(ToDoList, new PVector(-35, 0)); //Command 35 (Go home)


        indexDone = -1;    // Index in to-do list of last action performed
        indexDrawn = -1;   // Index in to-do list of last to-do element drawn to screen

        drawToDoList();

        Paused = false;
        pause();
    }

    void drawToDoList() {
        // Erase all painting on main image background, and draw the existing "ToDo" list
        // on the off-screen buffer.

        int j = ToDoList.length;
        float x1, x2, y1, y2;

        float brightness;
        int white = color(255, 255, 255);

        if ((indexDrawn + 1) < j) {

            // Ready the offscreen buffer for drawing onto
            offScreen.beginDraw();

            if (indexDrawn < 0) {
                offScreen.image(imgBackground, 0, 0, 800, 631);  // Copy original background image into place!

                offScreen.noFill();
                offScreen.strokeWeight(0.5f);

                if (PaperSizeA4) {
                    offScreen.stroke(128, 128, 255);  // Light Blue: A4
                    float rectW = PixelsPerInch * 297 / 25.4f;
                    float rectH = PixelsPerInch * 210 / 25.4f;
                    offScreen.rect((float) (MousePaperLeft), (float) (MousePaperTop), rectW, rectH);
                } else {
                    offScreen.stroke(255, 128, 128); // Light Red: US Letter
                    float rectW = PixelsPerInch * 11.0f;
                    float rectH = PixelsPerInch * 8.5f;
                    offScreen.rect((float) (MousePaperLeft), (float) (MousePaperTop), rectW, rectH);
                }
            } else
                offScreen.image(imgMain, 0, 0);

            offScreen.strokeWeight(1);
            //offScreen.stroke(PenColor);

            brightness = 0f;
            int DoneColor = lerpColor(PenColor, white, brightness);

            brightness = 0.8f;
            int ToDoColor = lerpColor(PenColor, white, brightness);


            x1 = 0;
            y1 = 0;

            boolean virtualPenDown = false;

            int index = 0;
            if (index < 0)
                index = 0;
            while (index < j) {
                PVector toDoItem = ToDoList[index];

                x2 = toDoItem.x;
                y2 = toDoItem.y;

                if (x2 >= 0) {
                    if (virtualPenDown) {
                        if (index < indexDone)
                            offScreen.stroke(DoneColor);
                        else
                            offScreen.stroke(ToDoColor);

                        offScreen.line(x1, y1, x2, y2); // Preview lines that are not yet on paper

                        //println("Draw line: "+str(x1)+", "+str(y1)+", "+str(x2) + ", "+str(y2));

                        x1 = x2;
                        y1 = y2;
                    } else {
                        //println("Pen up move");
                        x1 = x2;
                        y1 = y2;
                    }
                } else {
                    int x3 = -1 * round(x2);
                    if (x3 == 30) {
                        virtualPenDown = false;
                        //println("pen up");
                    } else if (x3 == 31) {
                        virtualPenDown = true;
                        //println("pen down");
                    } else if (x3 == 35) {// Home;  MoveToXY(0, 0); Do not draw home moves.
                        //if (virtualPenDown)
                        //offScreen.line(x1, y1, 0, 0); // Preview lines that are not yet on paper
                        x1 = 0;
                        y1 = 0;
                    }
                }


                index++;
            }


            offScreen.endDraw();

            imgMain = offScreen.get(0, 0, offScreen.width, offScreen.height);
        }
    }

    /**
     * Loop check functions (not for drawing)
     */


    boolean serviceBrush() {
        // Manage processes of getting paint, water, and cleaning the brush,
        // as well as general lifts and moves.  Ensure that we allow time for the
        // brush to move, and wait respectfully, without local wait loops, to
        // ensure good performance for the artist.

        // Returns true if servicing is still taking place, and false if idle.

        boolean serviceStatus = false;

        int waitTime = NextMoveTime - millis();
        if (waitTime >= 0) {
            serviceStatus = true;
            // We still need to wait for *something* to finish!
        } else {
            if (raiseBrushStatus >= 0) {
                raiseBrush();
                serviceStatus = true;
            } else if (lowerBrushStatus >= 0) {
                lowerBrush();
                serviceStatus = true;
            } else if (moveStatus >= 0) {
                MoveToXY(); // Perform next move, if one is pending.
                serviceStatus = true;
            }
        }
        return serviceStatus;
    }

    // Manage checking if the brush needs servicing, and moving to the next path
    void checkServiceBrush() {

        if (serviceBrush() == false)

            if (millis() > NextMoveTime) {

                boolean actionItem = false;
                int intTemp = -1;
                float inputTemp = -1.0f;
                PVector toDoItem;

                if ((ToDoList.length > (indexDone + 1)) && (Paused == false)) {
                    actionItem = true;
                    toDoItem = ToDoList[1 + indexDone];
                    inputTemp = toDoItem.x;
                    indexDone++;
                }

                if (actionItem) {  // Perform next action from ToDoList::

                    if (inputTemp >= 0) { // Move the carriage to draw a path segment!

                        toDoItem = ToDoList[indexDone];
                        float x2 = toDoItem.x;
                        float y2 = toDoItem.y;

                        int x1 = round((x2 - (float) (MousePaperLeft)) * MotorStepsPerPixel);
                        int y1 = round((y2 - (float) (MousePaperTop)) * MotorStepsPerPixel);

                        MoveToXY(x1, y1);
                        //println("Moving to: " + str(x2) + ", " + str(y2));

                        if (lastPosition.x == -1) {
                            lastPosition = toDoItem;
                            //println("Starting point: Init.");
                        }

                        lastPosition = toDoItem;

          /*
           IF next item in ToDoList is ALSO a move, then calculate the next move and queue it to the EBB at this time.
           Save the duration of THAT move as "SubsequentWaitTime."

           When the first (pre-existing) move completes, we will check to see if SubsequentWaitTime is defined (i.e., >= 0).
           If SubsequentWaitTime is defined, then (1) we add that value to the NextMoveTime:

           NextMoveTime = millis() + SubsequentWaitTime;
           SubsequentWaitTime = -1;

           We also (2) queue up that segment to be drawn.

           We also (3) queue up the next move, if there is one that could be queued.

           */
                    } else {
                        intTemp = round(-1 * inputTemp);

                        if ((intTemp > 9) && (intTemp < 20)) {  // Change paint color
                            intTemp -= 10;
                        } else if (intTemp == 30) {
                            raiseBrush();
                        } else if (intTemp == 31) {
                            lowerBrush();
                        } else if (intTemp == 35) {
                            MoveToXY(0, 0);
                        }
                    }
                }
            }
    }

    // Manage checking mouse position for highlights
    void checkHighlights() {
        boolean doHighlightRedraw = false;

        // Manage highlighting of text buttons
        if ((mouseY >= MousePaperBottom) || (mouseY < MousePaperTop)) {
            if ((mouseY <= height) && (mouseX >= (MousePaperLeft - 50))) {
                redrawButtons();
            }
        }


        if (doHighlightRedraw) {
            //redrawHighlight();
        }
    }

    /**
     * AxiDraw control functions
     */

    void raiseBrush() {
        int waitTime = NextMoveTime - millis();
        if (waitTime > 0) {
            raiseBrushStatus = 1; // Flag to raise brush when no longer busy.
        } else {
            if (BrushDown == true) {
                if (SerialOnline) {
                    myPort.write("SP,0," + str(delayAfterRaisingBrush) + "\r");
                    BrushDown = false;
                    NextMoveTime = millis() + delayAfterRaisingBrush;
                }
                //      if (debugMode) println("Raise Brush.");
            }
            raiseBrushStatus = -1; // Clear flag.
        }
    }


    void lowerBrush() {
        int waitTime = NextMoveTime - millis();
        if (waitTime > 0) {
            lowerBrushStatus = 1;  // Flag to lower brush when no longer busy.
            // delay (waitTime);  // Wait for prior move to finish:
        } else {
            if (BrushDown == false) {
                if (SerialOnline) {
                    myPort.write("SP,1," + str(delayAfterLoweringBrush) + "\r");

                    BrushDown = true;
                    NextMoveTime = millis() + delayAfterLoweringBrush;
                    //lastPosition = new PVector(-1,-1);
                }
                //      if (debugMode) println("Lower Brush.");
            }
            lowerBrushStatus = -1; // Clear flag.
        }
    }


    void MoveRelativeXY(int xD, int yD) {
        // Change carriage position by (xDelta, yDelta), with XY limit checking, time management, etc.

        int xTemp = MotorX + xD;
        int yTemp = MotorY + yD;

        MoveToXY(xTemp, yTemp);
    }


    void MoveToXY(int xLoc, int yLoc) {
        MoveDestX = xLoc;
        MoveDestY = yLoc;

        MoveToXY();
    }

    void MoveToXY() {
        int traveltime_ms;

        // Absolute move in motor coordinates, with XY limit checking, time management, etc.
        // Use MoveToXY(int xLoc, int yLoc) to set destinations.

        int waitTime = NextMoveTime - millis();
        if (waitTime > 0) {
            moveStatus = 1;  // Flag this move as not yet completed.
        } else {
            if ((MoveDestX < 0) || (MoveDestY < 0)) {
                // Destination has not been set up correctly.
                // Re-initialize varaibles and prepare for next move.
                MoveDestX = -1;
                MoveDestY = -1;
            } else {

                moveStatus = -1;
                if (MoveDestX > MotorMaxX)
                    MoveDestX = MotorMaxX;
                else if (MoveDestX < MotorMinX)
                    MoveDestX = MotorMinX;

                if (MoveDestY > MotorMaxY)
                    MoveDestY = MotorMaxY;
                else if (MoveDestY < MotorMinY)
                    MoveDestY = MotorMinY;

                int xD = MoveDestX - MotorX;
                int yD = MoveDestY - MotorY;

                if ((xD != 0) || (yD != 0)) {

                    MotorX = MoveDestX;
                    MotorY = MoveDestY;

                    int MaxTravel = max(abs(xD), abs(yD));
                    traveltime_ms = (int) (floor((float) (1000 * MaxTravel) / MotorSpeed));

                    NextMoveTime = millis() + traveltime_ms - ceil(1000 / frameRate);
                    // Important correction-- Start next segment sooner than you might expect,
                    // because of the relatively low framerate that the program runs at.

                    if (SerialOnline) {
                        if (reverseMotorX)
                            xD *= -1;
                        if (reverseMotorY)
                            yD *= -1;

                        myPort.write("XM," + str(traveltime_ms) + "," + str(xD) + "," + str(yD) + "\r");
                        //General command "XM,duration,axisA,axisB<CR>"
                    }

                    // Calculate and animate position location cursor
                    int[] pos = getMotorPixelPos();
                    float sec = traveltime_ms / 1000.0f;

                    Ani.to(this, sec, "MotorLocatorX", pos[0]);
                    Ani.to(this, sec, "MotorLocatorY", pos[1]);

                    //        if (debugMode) println("Motor X: " + MotorX + "  Motor Y: " + MotorY);
                }
            }
        }

        // Need
        // SubsequentWaitTime
    }


    void MotorsOff() {
        if (SerialOnline) {
            myPort.write("EM,0,0\r");  //Disable both motors

            //    if (debugMode) println("Motors disabled.");
        }
    }

    void quitApp() {  // ***** QUIT *****

        if (SerialOnline) {
            myPort.clear();
            myPort.stop();
        }

        exit();
    }

    public static void main(String[] args) {
        PApplet.main("MainClass", args);
    }


    @Override
    public void settings() {
        size(800, 631);
    }

    @Override
    public void setup() {
        R = new Renderer(this);
        scenes = new ArrayList<Scene>();
        //scenes.add(new Scene1());
        //scenes.add(new Scene2());
        scenes.add(new Scene3());
        //pixelDensity(2);

        Ani.init(this); // Initialize animation library
        Ani.setDefaultEasing(Ani.LINEAR);

        firstPath = true;

        //offScreen = createGraphics(800, 631, JAVA2D);
        offScreen = createGraphics(800, 631);


        //// Allow frame to be resized?
        //  if (frame != null) {
        //    frame.setResizable(true);
        //  }

        surface.setTitle("OZ based on AxiGen");

        if (PaperSizeA4) {
            MousePaperRight = round(MousePaperLeft + PixelsPerInch * 297 / 25.4f);
            MousePaperBottom = round(MousePaperTop + PixelsPerInch * 210 / 25.4f);
        } else {
            MousePaperRight = round(MousePaperLeft + PixelsPerInch * 11.0f);
            MousePaperBottom = round(MousePaperTop + PixelsPerInch * 8.5f);
        }


        shiftKeyDown = false;

        frameRate(60);  // sets maximum speed only


        MotorMinX = 0;
        MotorMinY = 0;
        MotorMaxX = (int) (floor((float) (MousePaperRight - MousePaperLeft) * MotorStepsPerPixel));
        MotorMaxY = (int) (floor((float) (MousePaperBottom - MousePaperTop) * MotorStepsPerPixel));

        lastPosition = new PVector(-1, -1);

        ServoUp = 7500 + 175 * ServoUpPct;    // Brush UP position, native units
        ServoPaint = 7500 + 175 * ServoPaintPct;   // Brush DOWN position, native units.


        // Button setup

        font_ML16 = loadFont("Miso-Light-16.vlw");
        font_CB = loadFont("Miso-20.vlw");


        int xbutton = MousePaperLeft + 100;
        int ybutton = MousePaperBottom + 20;

        pauseButton = new SimpleButton(this, "Start", xbutton, MousePaperBottom + 20, font_CB, 20, TextColor, TextHighLight);
        xbutton += 60;

        brushLabel = new SimpleButton(this, "Pen:", xbutton, ybutton, font_CB, 20, LabelColor, LabelColor);
        xbutton += 45;
        brushUpButton = new SimpleButton(this, "Up", xbutton, ybutton, font_CB, 20, TextColor, TextHighLight);
        xbutton += 22;
        brushDownButton = new SimpleButton(this, "Down", xbutton, ybutton, font_CB, 20, TextColor, TextHighLight);
        xbutton += 44;

        parkButton = new SimpleButton(this, "Park", xbutton, ybutton, font_CB, 20, TextColor, TextHighLight);
        xbutton += 60;

        motorLabel = new SimpleButton(this, "Motors:", xbutton, ybutton, font_CB, 20, LabelColor, LabelColor);
        xbutton += 55;
        motorOffButton = new SimpleButton(this, "Off", xbutton, ybutton, font_CB, 20, TextColor, TextHighLight);
        xbutton += 30;
        motorZeroButton = new SimpleButton(this, "Zero", xbutton, ybutton, font_CB, 20, TextColor, TextHighLight);
        xbutton += 70;
        clearButton = new SimpleButton(this, "Clear All", xbutton, MousePaperBottom + 20, font_CB, 20, TextColor, TextHighLight);
        xbutton += 80;
        replayButton = new SimpleButton(this, "Replay All", xbutton, MousePaperBottom + 20, font_CB, 20, TextColor, TextHighLight);

        xbutton = MousePaperLeft + 30;
        ybutton = 30;

        quitButton = new SimpleButton(this, "Quit", xbutton, ybutton, font_CB, 20, LabelColor, TextHighLight);

        xbutton = 655;

        urlButton = new SimpleButton(this, "AxiDraw.com", xbutton, ybutton, font_CB, 20, LabelColor, TextHighLight);

        UIMessage = new SimpleButton(this, "Welcome to AxiGen! Hold 'h' key for help!",
                MousePaperLeft, MousePaperTop - 5, font_CB, 20, LabelColor, LabelColor);


        UIMessage.label = "Searching For ... ";
        UIMessageExpire = millis() + 25000;

        rectMode(CORNERS);


        MotorX = 0;
        MotorY = 0;

        ToDoList = new PVector[0];

        //ToDoList = new int[0];
        PVector cmd = new PVector(-35, 0);   // Command code: Go home (0,0)
        ToDoList = (PVector[]) append(ToDoList, cmd);


        indexDone = -1;    // Index in to-do list of last action performed
        indexDrawn = -1;   // Index in to-do list of last to-do element drawn to screen

        raiseBrushStatus = -1;
        lowerBrushStatus = -1;
        moveStatus = -1;
        MoveDestX = -1;
        MoveDestY = -1;


        Paused = true;
        BrushDownAtPause = false;

        // Set initial position of indicator at carriage minimum 0,0
        int[] pos = getMotorPixelPos();

        background(255);
        MotorLocatorX = pos[0];
        MotorLocatorY = pos[1];

        NextMoveTime = millis();
        imgBackground = loadImage(BackgroundImageName);  // Load the image into the program

        drawToDoList();
        redrawButtons();
        redrawHighlight();
        redrawLocator();
    }


    @Override
    public void draw() {

        if (debugMode) {
            frame.setTitle("AxiGen      " + (int) (frameRate) + " fps");
        }

        drawToDoList();

        // NON-DRAWING LOOP CHECKS ==========================================

        if (doSerialConnect == false)
            checkServiceBrush();


        checkHighlights();

        if (UIMessage.label != "")
            if (millis() > UIMessageExpire) {

                UIMessage.displayColor = lerpColor(UIMessage.displayColor, color(242), .5f);
                UIMessage.highlightColor = UIMessage.displayColor;

                if (millis() > (UIMessageExpire + 500)) {
                    UIMessage.label = "";
                    UIMessage.displayColor = LabelColor;
                }
                redrawButtons();
            }


        // ALL ACTUAL DRAWING ==========================================

        if (hKeyDown) {  // Help display
            image(loadImage(HelpImageName), 0, 0, 800, 631);


            println("HELP requested");
        } else {

            image(imgMain, 0, 0, width, height);    // Draw Background image  (incl. paint paths)

            // Draw buttons image
            image(imgButtons, 0, 0);

            // Draw highlight image
            image(imgHighlight, 0, 0);

            // Draw locator crosshair at xy pos, less crosshair offset
            image(imgLocator, MotorLocatorX - 10, MotorLocatorY - 15);
        }


        if (doSerialConnect) {
            // FIRST RUN ONLY:  Connect here, so that

            doSerialConnect = false;

            scanSerial();

            if (SerialOnline) {
                myPort.write("EM,1,1\r");  //Configure both steppers in 1/16 step mode

                // Configure brush lift servo endpoints and speed
                myPort.write("SC,4," + str(ServoPaint) + "\r");  // Brush DOWN position, for painting
                myPort.write("SC,5," + str(ServoUp) + "\r");  // Brush UP position

                myPort.write("SC,10,65535\r"); // Set brush raising and lowering speed.

                // Ensure that we actually raise the brush:
                BrushDown = true;
                raiseBrush();

                UIMessage.label = "Welcome to AxiGen!  Hold 'h' key for help!";
                UIMessageExpire = millis() + 5000;
                redrawButtons();
            } else {
                println("Now entering offline simulation mode.\n");

                UIMessage.label = "AxiDraw not found.  Entering Simulation Mode. ";
                UIMessageExpire = millis() + 5000;
                redrawButtons();
            }
        }
    }


    @Override
    public void pause()
    {
        //println("Pause button");
        pauseButton.displayColor = TextColor;
        if (Paused)
        {
            Paused = false;
            pauseButton.label = "Pause";


            if (BrushDownAtPause)
            {
                int waitTime = NextMoveTime - millis();
                if (waitTime > 0)
                {
                    delay (waitTime);  // Wait for prior move to finish:
                }

                if (BrushDown) {
                    raiseBrush();
                }

                waitTime = NextMoveTime - millis();
                if (waitTime > 0)
                {
                    delay (waitTime);  // Wait for prior move to finish:
                }

                MoveToXY(xLocAtPause, yLocAtPause);

                waitTime = NextMoveTime - millis();
                if (waitTime > 0)
                {
                    delay (waitTime);  // Wait for prior move to finish:
                }

                lowerBrush();
            }
        } else
        {
            Paused = true;
            pauseButton.label = "Resume";
            //TextColor


            if (BrushDown) {
                BrushDownAtPause = true;
                raiseBrush();
            } else
                BrushDownAtPause = false;

            xLocAtPause = MotorX;
            yLocAtPause = MotorY;
        }

        redrawButtons();
    }

    @Override
    public void mousePressed() {
        boolean doHighlightRedraw = false;

        //The mouse button was just pressed!  Let's see where the user clicked!

        if ((mouseX >= MousePaperLeft) && (mouseX <= MousePaperRight) && (mouseY >= MousePaperTop) && (mouseY <= MousePaperBottom)) {

            for(Scene s:scenes){
                s.draw(R, MousePaperLeft, MousePaperTop, MousePaperRight, MousePaperBottom);
            }

            doHighlightRedraw = true;
        }


        if (doHighlightRedraw) {
            redrawLocator();
            redrawHighlight();
        }


        if (pauseButton.isSelected())
            pause();
        else if (brushUpButton.isSelected()) {

            if (Paused)
                raiseBrush();
            else {
                ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); // Command 30 (raise pen)
            }
        } else if (brushDownButton.isSelected()) {

            if (Paused)
                lowerBrush();
            else {
                ToDoList = (PVector[]) append(ToDoList, new PVector(-31, 0)); // Command 31 (lower pen)
            }
        } else if (urlButton.isSelected()) {
            link("http://axidraw.com");
        } else if (parkButton.isSelected()) {

            if (Paused) {
                raiseBrush();
                MoveToXY(0, 0);
            } else {
                ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); // Command 30 (raise pen)
                ToDoList = (PVector[]) append(ToDoList, new PVector(-35, 0)); // Command 35 (go home)
            }
        } else if (motorOffButton.isSelected())
            MotorsOff();
        else if (motorZeroButton.isSelected())
            zero();
        else if (clearButton.isSelected())
            clearall();
        else if (replayButton.isSelected()) {
            // Clear indexDone to "zero" (actually, -1, since even element 0 is not "done.")   & redraw to-do list.

            indexDone = -1;    // Index in to-do list of last action performed
            indexDrawn = -1;   // Index in to-do list of last to-do element drawn to screen

            drawToDoList();
        } else if (quitButton.isSelected())
            quitApp();
    }

    @Override
    public void keyReleased()
    {

        if (key == CODED) {

            if (keyCode == UP) keyup = false;
            if (keyCode == DOWN) keydown = false;
            if (keyCode == LEFT) keyleft = false;
            if (keyCode == RIGHT) keyright = false;

            if (keyCode == SHIFT) {

                shiftKeyDown = false;
            }
        } else
            key = Character.toLowerCase(key);

        if ( key == 'h')  // display help
        {
            hKeyDown = false;
        }
    }



    @Override
    public void keyPressed()
    {


        if (key == CODED) {

            // Arrow keys are used for nudging, with or without shift key.

            if (keyCode == UP)
            {
                keyup = true;
            }
            if (keyCode == DOWN)
            {
                keydown = true;
            }
            if (keyCode == LEFT) keyleft = true;
            if (keyCode == RIGHT) keyright = true;
            if (keyCode == SHIFT) shiftKeyDown = true;
        } else
        {
            key = Character.toLowerCase(key);
            println("Key pressed" + key);

            if ( key == 'b')   // Toggle brush up or brush down with 'b' key
            {
                if (BrushDown)
                    raiseBrush();
                else
                    lowerBrush();
            }

            if ( key == 'z')  // Zero motor coordinates
                zero();

            if ( key == 'c')  // Zero motor coordinates
                clearall();

            if ( key == ' ')  //Space bar: Pause
                pause();

            if ( key == 'q')  // Move home (0,0)
            {
                raiseBrush();
                MoveToXY(0, 0);
            }

            if ( key == 'h')  // display help
            {
                hKeyDown = true;
                println("HELP requested");
            }

            if ( key == 't')  // Disable motors, to manually move carriage.
                MotorsOff();

            if ( key == '1')
                MotorSpeed = 500;
            if ( key == '2')
                MotorSpeed = 1000;
            if ( key == '3')
                MotorSpeed = 2000;
            if ( key == '4')
                MotorSpeed = 3000;
            if ( key == '5')
                MotorSpeed = 4000;
            if ( key == '6')
                MotorSpeed = 4500;
            if ( key == '7')
                MotorSpeed = 5000;
            if ( key == '8')
                MotorSpeed = 5500;
            if ( key == '9')
                MotorSpeed = 6000;
        }
    }
}
