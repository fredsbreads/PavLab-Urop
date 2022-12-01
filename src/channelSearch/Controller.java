package channelSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Controller {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<List<Integer>> candidatePath = new ArrayList<>();
    private List<List<Integer>> traversedPath = new ArrayList<>();
    private boolean done = false;
    private List<List<Integer>> grid = new ArrayList<>();
    
    
    public Controller(Set<List<Integer>> coordinates, List<List<Integer>> depthGrid) {
        for (List<Integer> coordinate: coordinates) {
            Vehicle vehicle = new Vehicle(coordinate.get(0), coordinate.get(1), depthGrid);
            vehicles.add(vehicle);
        }
        
        grid = depthGrid;
    }
    
    public List<List<Integer>> newPositions() {
        //for multiple vehicles
//        List<List<Integer>> positions = new ArrayList<>();
//        for (Vehicle vehicle: vehicles) {
//            vehicle.move();
//            positions.add(vehicle.getPosition());
//        }
//        return positions;
        
        Vehicle vehicle = vehicles.get(0);
        vehicle.move();
        
        
        
        
        
        int y = vehicle.getPosition().get(0);
        int x = vehicle.getPosition().get(1);
        
        if (x == 0) {
            candidatePath.clear();
        }
        
        if (x == 0 || grid.get(vehicle.getPosition().get(0)).get(vehicle.getPosition().get(1)) >= 4) {            
            candidatePath.add(vehicle.getPosition());
        }
        traversedPath.add(vehicle.getPosition());
        
        if (x == grid.get(0).size() - 1) {
            done = true;
        }
        else if (x == 0 && y == grid.size() - 1) {
            done = true;
        }
        
        return List.of(vehicle.getPosition());
    }
    
    public boolean isDone() {
        return done;
    }
    
    public List<List<Integer>> getPath() {
        return candidatePath;
    }
    
    public List<List<Integer>> getTraversedPath() {
        return traversedPath;
    }
    
    
}
