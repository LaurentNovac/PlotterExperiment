class Renderer {
  void point(float x, float y) {
    ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (raise pen)
    ToDoList = (PVector[]) append(ToDoList, new PVector(x, y)); 
    ToDoList = (PVector[]) append(ToDoList, new PVector(-31, 0)); //Command 31 (lower pen)
    ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (raise pen)
  }

  void ellipse(float x, float y, float radius) {
    ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (raise pen)
    ToDoList = (PVector[]) append(ToDoList, new PVector(x, y)); 

    ToDoList = (PVector[]) append(ToDoList, new PVector(-31, 0)); //Command 31 (lower pen)
    float xPos = x;
    float yPos = y;

    int deltaAngle = 20;
    int N = 360/deltaAngle;
    for (int i = 1; i < N; i++) {
      float r = radians(i*deltaAngle);
      xPos = xPos + (radius * cos(r));
      yPos = yPos + (radius * sin(r));   

      if ((xPos < MousePaperLeft) || (xPos > MousePaperRight))
        break;
      if ((yPos < MousePaperTop) || (yPos > MousePaperBottom))
        break;

      // Command Code: Move to (X,Y)
      ToDoList = (PVector[]) append(ToDoList, new PVector(xPos, yPos));
    }

    ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (raise pen)
  }
}

void example1(float xStart, float yStart, float xMin, float yMin, float xMax, float yMax) {
  Renderer R = new Renderer();
  float xPos = xStart;
  float yPos = yStart;

  ToDoList = (PVector[]) append(ToDoList, new PVector(-30, 0)); //Command 30 (raise pen)

  // Command Code: Move to first (X,Y) point
  ToDoList = (PVector[]) append(ToDoList, new PVector(xPos, yPos));

  for (int x = 0; x < 10; x++) {
    for (int y = 0; y < 10; y++) {
      float pX = map(x,0,10,xPos,min(xPos+80,xMax));
      float pY = map(y,0,10,yPos,min(yPos+80,yMax));
      
      R.ellipse(pX+noise(x), pY+noise(y), 1);
    }
  }
}
