package channelSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Controller {
    private List<Vehicle> vehicles = new ArrayList<>();
//    private List<List<Integer>> foundPaths = new ArrayList<>(); //all candidate paths from vehicles
//    private List<List<Integer>> traversedPaths = new ArrayList<>();//all areas explored by vehicles
    private boolean done = false;
    private List<List<Integer>> grid = new ArrayList<>();
    private boolean single; //true if only one vehicle is searching
    
    private boolean firstOverlap = false;//tracks whether the first vehicle has come across the second vehicle's path
    private boolean secondOverlap = false;//tracks whether the second vehicle has come across the first vehicle's path
    //^^if both true then terminate, they've run into each other
    
//    public Controller(Set<List<Integer>> coordinates, List<List<Integer>> depthGrid) {
//        for (List<Integer> coordinate: coordinates) {
//            Vehicle vehicle = new Vehicle(coordinate.get(0), coordinate.get(1), depthGrid);
//            vehicles.add(vehicle);
//        }
//        
//        grid = depthGrid;
//    }
    
    public Controller(boolean single, List<List<Integer>> depthGrid) {
        //hacky reconstruct for two vehicles
    	
    	vehicles.add(new Vehicle(true, 0, 0, depthGrid));
        if (! single) {
            vehicles.add(new Vehicle(false, depthGrid.size()-1, 0, depthGrid));
        }
        
        this.single = single;
        grid = depthGrid;
    }
    
    
    
    public List<List<Integer>> newPositions() { //finding vehicle positions shouldn't rely on them to move but currently does
        //for multiple vehicles
        List<List<Integer>> positions = new ArrayList<>();
        for (Vehicle vehicle: vehicles) {
            vehicle.move();
            positions.add(vehicle.getPosition());
            done = done || vehicle.isDone();
        }
        
        if (! single && ! done) { //controller is done if both vehicles have crossed paths
        	Vehicle first = vehicles.get(0);
        	Vehicle second = vehicles.get(1);
        	
        	if (! firstOverlap && second.getTraversedPath().contains(first.getPosition())) { //should probably be a set instead of a list for speedup
        		firstOverlap = true;
        	}
        	if (! secondOverlap && first.getTraversedPath().contains(second.getPosition())) {
        		secondOverlap = true;
        	}
        	
        	if (firstOverlap && secondOverlap) {
        		done = true;
        	}
        }
        
        
        
        
        return positions;
        
//        Vehicle vehicle = vehicles.get(0);
//        vehicle.move();
        
        
        
        
        
//        int y = vehicle.getPosition().get(0);
//        int x = vehicle.getPosition().get(1);
//        
//        if (x == 0) {
//            candidatePath.clear();
//        }
//        
//        if (x == 0 || grid.get(vehicle.getPosition().get(0)).get(vehicle.getPosition().get(1)) >= 4) {            
//            candidatePath.add(vehicle.getPosition());
//        }
//        traversedPath.add(vehicle.getPosition());
//        
//        if (x == grid.get(0).size() - 1) {
//            done = true;
//        }
//        else if (x == 0 && y == grid.size() - 1) {
//            done = true;
//        }
        
//        return List.of(vehicle.getPosition());
    	
    }
    
    public boolean isDone() {
        return done;
    }
    
    public List<List<Integer>> getFoundPaths() { //should only be called once controller is done
    	List<List<Integer>> foundPaths = new ArrayList<>();
    	for (Vehicle vehicle: vehicles) {
    		foundPaths.addAll(vehicle.getFoundPath());
    	}
        return foundPaths;
    }
    
    public List<List<Integer>> getTraversedPaths() {//should only be called once controller is done
    	List<List<Integer>> traversedPaths = new ArrayList<>();
    	for (Vehicle vehicle: vehicles) {
    		traversedPaths.addAll(vehicle.getTraversedPath());
    	}
        return traversedPaths;
    }
}
