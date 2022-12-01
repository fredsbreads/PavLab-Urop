package channelSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Vehicle {
    private int xPosition;
    private int yPosition;

    private List<List<Integer>> order = List.of(List.of(-1,-1), List.of(-1,0), List.of(-1,1), List.of(0,1), List.of(1,1), List.of(1,0), List.of(1,-1), List.of(0,-1));
//    private List<List<Integer>> order = List.of(List.of(0,-1), List.of(1,-1), List.of(1,0), List.of(1,1), List.of(0,1), List.of(-1,1), List.of(-1,0), List.of(-1,-1));
    //^order uses y increases downwards
    
    private List<List<Integer>> localGrid = new ArrayList<>();
    private List<List<Integer>> depthGrid;
    
    private List<Integer> lastVitalLocation; //last vital (deep or start channel) location navigated across
    private List<Integer> lastDirection; //direction that was faced when lastDeepLocation was entered
    
    private List<List<Integer>> queue = new ArrayList<>();
    private boolean clockwise; //tracks whether vehicle is top or bottom
    
    private boolean done; //true if vehicle is done
    
    private List<List<Integer>> candidatePath = new ArrayList<>();
    private List<List<Integer>> traversedPath = new ArrayList<>();
    
    
    //depthGrid is exposed
    public Vehicle(boolean top, int y, int x, List<List<Integer>> depthGrid) { //probably safe to assume that grid is at least 2x2
        yPosition = y;
        xPosition = x;
        
        
//        List<Integer> list = new ArrayList<Integer>(Collections.nCopies(60, 0));
//        localGrid = new ArrayList<List<Integer>>(Collections.nCopies(depthGrid.size(), new ArrayList<Integer>(Collections.nCopies(depthGrid.get(0).size(), 0))));
        for (int i = 0; i < depthGrid.size(); i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < depthGrid.get(0).size(); j++) {
                row.add(0);
            }
            localGrid.add(row);
        }
        
        this.clockwise = top;
        this.depthGrid = depthGrid;
        
        if (this.clockwise) {
        	this.lastVitalLocation = List.of(0, 0);
        	this.lastDirection = List.of(1,1);
        }
        else {
        	this.lastVitalLocation = List.of(depthGrid.size()-1, 0);
        	this.lastDirection = List.of(-1, 1);
        }
    }
    
    public List<Integer> getPosition() { //y, x
        List<Integer> position = new ArrayList<>();
        position.add(yPosition);
        position.add(xPosition);
        return position;
    }
    
    private List<List<Integer>> queueOrder() {
    	
        int index = 0;
        for (List<Integer> orientation: order) {
            if (lastDirection.equals(orientation)) {
                break;
            }
            index += 1;
        }
        if (clockwise) {
        	index -= 2;
        	if (index < 0) {
        		index += 8;
        	}
        }
        else {
        	index += 2;
        	if (index >= 8) {
        		index -= 8;
        	}
        }
    	
    	List<List<Integer>> queueOrder = new ArrayList<>();
    	
    	if (clockwise) {
    		for (int i = index; i < 8; i++) {
    			queueOrder.add(order.get(i));
    		}
    		for (int i = 0; i < index - 1; i++) {
    			queueOrder.add(order.get(i));
    		}    		
    	}
    	else {
    		for (int i = index; i >= 0; i--) {
    			queueOrder.add(order.get(i));
    		}
    		for (int i = 7; i > index + 1; i--) {
    			queueOrder.add(order.get(i));
    		}    
    	}
    	return queueOrder;
    }
    
    
    /**
     * @return a list of coordinates that would entail following the corresponding edge (top or bottom), DOES NOT EXCLUDE NODES PREVIOUSLY VISITED
     */
    public void createQueue() {
        //create legal list
        //does not exclude coordinates until late to allow for local map to be updated later
        //move in single units towards next item in queue (implemented this way for the edge cases where top queue item isn't immediately touching current position)
        queue.clear();
        
        for (List<Integer> orientation: queueOrder()) {        
            int newy = yPosition + orientation.get(0);
            int newx = xPosition + orientation.get(1);
            
            if (newy >= 0 && newx >= 0 && newy < depthGrid.size() && newx < depthGrid.get(0).size()) {
                queue.add(List.of(newy, newx));
            }
        }
    }
    
    
    public void move() {
        localGrid.get(yPosition).set(xPosition, depthGrid.get(yPosition).get(xPosition)); //update local grid
        
        //different end conditions depending on clockwise orientation
        if (clockwise) {
        	if ((yPosition == depthGrid.size() - 1 && xPosition == 0) || xPosition == depthGrid.get(0).size() - 1) { //dont move if terminated
        		return;
        	}        	
        }
        else {
        	if ((yPosition == 0 && xPosition == 0) || xPosition == depthGrid.get(0).size() - 1) { //dont move if terminated
        		return;
        	}   
        }
        
        if (queue.isEmpty()) {//should only be true at the very start

            createQueue();
        }
//    	System.out.println(queue);

        List<Integer> destination = queue.get(0);
        
        if (yPosition == destination.get(0) && xPosition == destination.get(1)) {
            if (xPosition == 0 || depthGrid.get(yPosition).get(xPosition) >= 4) {
//            	System.out.println(lastVitalLocation);
//            	System.out.println(destination);
                lastDirection = List.of(destination.get(0) - lastVitalLocation.get(0), destination.get(1) - lastVitalLocation.get(1));
                
                if (! order.contains(lastDirection)) {
                    System.out.print("BUGGGG");
//                    System.out.println(lastDirection);
                }
                lastVitalLocation = destination;
                
                createQueue();
            }
            
            //skips nodes which are known not to be on path
            destination = queue.get(0);
            int value = localGrid.get(destination.get(0)).get(destination.get(1)); //destination depth
            while (value > 0 && value <= 3) { //true if known to be shallow, false otherwise
                queue.remove(0);
                destination = queue.get(0);
                value = localGrid.get(destination.get(0)).get(destination.get(1));
//                System.out.println(queue);
//                System.out.println(value);
            }            
        }

        
        //ASSUMES QUEUE CAN NEVER BE EMPTY
        inch(queue.get(0));
        
        
        
        if (xPosition == 0) {
            candidatePath.clear();
        }
        
        if (xPosition == 0 || depthGrid.get(yPosition).get(xPosition) >= 4) {            
            candidatePath.add(getPosition());
        }
        traversedPath.add(getPosition());
        
        if (xPosition == depthGrid.get(0).size() - 1) {
            done = true;
        }
        else if (clockwise && xPosition == 0 && yPosition == depthGrid.size() - 1) {
        	done = true;
        }
        else if (! clockwise && xPosition == 0 && yPosition == 0) {
    		done = true;
    }
    }
    
    /**
     * move one cell closer to coordinate (includes diagonal)
     * @param coordinate
     */
    public void inch(List<Integer> coordinate) {
        if (yPosition < coordinate.get(0)) {
            yPosition++;
        }
        else if (yPosition > coordinate.get(0)) {
            yPosition--;
        }
        
        if (xPosition < coordinate.get(1)) {
            xPosition++;
        }
        else if (xPosition > coordinate.get(1)) {
            xPosition--;
        }
    }
    
    public List<List<Integer>> getFoundPath() {
        return candidatePath;
    }
    
    public List<List<Integer>> getTraversedPath() {
        return traversedPath;
    }
    
    public boolean isDone() {
    	return done;
    }
    
//    public List<List<Integer>> neighboringCells() { //includes self as neighbor
//      List<Integer> xDeltas = new ArrayList<>();
//      List<Integer> yDeltas = new ArrayList<>();
//      
//      List<List<Integer>> neighbors = new ArrayList<>();
//
//      if (xPosition == 0) {
//          xDeltas.add(0);
//          xDeltas.add(1);
//      }
//      else if (xPosition == localGrid.get(0).size() - 1) {
//          xDeltas.add(-1);
//          xDeltas.add(0);
//      }
//      else {
//          xDeltas.add(-1);
//          xDeltas.add(0);
//          xDeltas.add(1);
//      }
//      
//      if (yPosition == 0) {
//          yDeltas.add(0);
//          yDeltas.add(1);
//      }
//      else if (yPosition == localGrid.size() - 1) {
//          yDeltas.add(-1);
//          yDeltas.add(0);
//      }
//      else {
//          yDeltas.add(-1);
//          yDeltas.add(0);
//          yDeltas.add(1);
//      }
//      
//      for (int xDelta: xDeltas) {
//          for (int yDelta: yDeltas) {
//              List<Integer> neighbor = new ArrayList<>();
//              neighbor.add(yPosition + yDelta);
//              neighbor.add(xPosition + xDelta);
//              neighbors.add(neighbor);
//          }
//      }
//      
//      return neighbors;
//  }
    
//    public void move() {
////        List<List<Integer>> neighbors = neighboringCells();
////        
////        Random random = new Random();
////        int randomIndex = random.nextInt(neighbors.size());
////        List<Integer> newPosition = neighbors.get(randomIndex);
////        
////        yPosition = newPosition.get(0);
////        xPosition = newPosition.get(1);
////        
////        //populate localGrid with all learned values
//        
//        if (xPosition == 0 && yPosition == depthGrid.size() - 1) { //terminate at bottom left
//            return;
//        }
//        
//        int index = 0;
//        for (List<Integer> orientation: order) {
//            if (direction.equals(orientation)) {
//                break;
//            }
//            index += 1;
//        }
//        
//        index -= 2;
//        if (index < 0) {
//            index += 8;
//        }
//        
//        boolean chosen = false;
//        
//        for (int i = index; i < 8; i++) {
//            List<Integer> orientation = order.get(i);
//            
//            int newy = yPosition + orientation.get(0);
//            int newx = xPosition + orientation.get(1);
//            
//            if (newy >= 0 && newx >= 0 && newy < depthGrid.size() && newx < depthGrid.get(0).size()) {
////                System.out.println(orientation);
//
////                List<Integer> candidatePosition = List.of(newy, newx);
//                if (depthGrid.get(newy).get(newx) >= 4) {
//                    if (xPosition == 0) {
//                        if (orientation.get(0) != 1 || (orientation.get(0) == -1 * direction.get(0) && orientation.get(1) == -1 * direction.get(1))) { //forces vehicle to move downwards (not the way it came) when in start channel
//                            yPosition += 1;
//                            direction = List.of(1,0);
//                            chosen = true;
//                            break;
//                        }
//                    }
//                    xPosition = newx;
//                    yPosition = newy;
//                    direction = orientation;
//                    chosen = true;
//                    break;
//                }
//            }
//        }
//        
//        if (! chosen) {
//            for (int i = 0; i < index; i++) { //can probably make index - 2 as cap
//                List<Integer> orientation = order.get(i);
//
//                int newy = yPosition + orientation.get(0);
//                int newx = xPosition + orientation.get(1);
//                
//                if (newy >= 0 && newx >= 0 && newy < depthGrid.size() && newx < depthGrid.get(0).size()) {
////                    List<Integer> candidatePosition = List.of(newy, newx);
//                    if (depthGrid.get(newy).get(newx) >= 4) {
//                        if (xPosition == 0) {
//                            if (orientation.get(0) != 1 || (orientation.get(0) == -1 * direction.get(0) && orientation.get(1) == -1 * direction.get(1))) { //forces vehicle to move downwards (not the way it came) when in start channel
//                                yPosition += 1;
//                                direction = List.of(1,0);
//                                chosen = true;
//                                break;
//                            }
//                        }
//                        xPosition = newx;
//                        yPosition = newy;
//                        direction = orientation;
//                        chosen = true;
//                        break;
//                    }
//                }
//            }
//           
//            
//        }
//        
//        if (! chosen) { // should only trigger on left channel
//            yPosition += 1;
//            direction = List.of(1,0);
//        }
//        
////        System.out.println(List.of(yPosition, xPosition, direction));
//        
//    }
}
