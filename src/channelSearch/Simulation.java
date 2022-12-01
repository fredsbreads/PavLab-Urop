package channelSearch;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Simulation extends JFrame {
    
    private List<List<Color>> colorGrid = new ArrayList<>();
    private List<List<Integer>> depthGrid = new ArrayList<>();
    private List<List<JPanel>> panelGrid = new ArrayList<>();
//    private Map
    
//    private static Map<Color, Integer> colorToDepth = Map.of(Color.white, 0, Color.cyan, 1, Color.green, 2, Color.yellow, 3, Color.orange, 4, Color.red, 5);
    private static Map<Integer, Color> depthToColor = Map.of(0, Color.white, 1, Color.cyan, 2, Color.green, 3, Color.yellow, 4, Color.orange, 5, Color.red);
    
    private boolean selecting = false;
    private Set<List<Integer>> selected = new HashSet<>();
    
    private boolean playing = false;
    private Timer timer;
    private boolean done = false;
    
    
//    private boolean running = false;
    private Controller coordinator;
    
    private JFrame window = this;
    private JPanel contentPane;
    private JButton playButton;
    
    public class SmartPanel extends JPanel{
        private int x;
        private int y;
        public SmartPanel(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getXIndex() {
            return this.x;
        }
        
        public int getYIndex() {
            return this.y;
        }
        
    }
    
    
    public Simulation(List<List<Integer>> depthMatrix, List<List<Color>> colorMatrix) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 1600, 900);

        contentPane = new JPanel();
//        contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        
        this.depthGrid = depthMatrix;
        this.colorGrid = colorMatrix;
        
        JButton selectButton = new JButton("Select Start Positions");
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selecting = !selecting;
                if (selecting) {
                    selectButton.setText("Stop Selecting");
                }
                else {
                    selectButton.setText("Select Start Positions");
                    coordinator = new Controller(selected, depthGrid); //new Coordinator created each time selecting is toggled off
                }
                
            }
        });
        selectButton.setBounds(6, 6, 160, 29);
        contentPane.add(selectButton);
        
        JButton stepButton = new JButton("Time Step");
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeStep();
            }
        });
        stepButton.setBounds(167, 6, 117, 29);
        contentPane.add(stepButton);
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                done = false;
                clear();
            }
        });
        resetButton.setBounds(425, 6, 117, 29);
        contentPane.add(resetButton);
        
        playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playing = !playing;
                
                if (playing) {
                    playButton.setText("Stop");
                    
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            timeStep();
                        }
                    }, 0, 100); //task, initial delay, repeat timer in milliseconds
                }
                else {
                    playButton.setText("Play");
                    timer.cancel();
                }
            }
        });
        playButton.setBounds(296, 6, 117, 29);
        contentPane.add(playButton);
        
        
        int height = colorMatrix.size();
        int length = colorMatrix.get(0).size();
        
        int y = 45;
        int increment = 800 / height;
        
        for (int j = 0; j < height; j++) {
            int x = 25;

            List<JPanel> panelRow = new ArrayList<>();
            List<Color> colorRow = colorMatrix.get(j);
            
            panelGrid.add(panelRow);
            for (int i = 0; i < length; i++) {
                SmartPanel panel = new SmartPanel(i, j);
//                JPanel panel = new JPanel();
                panel.setBackground(colorRow.get(i));
                panel.setBounds(x, y, increment, increment);
                panel.setBorder(new LineBorder(new Color(0, 0, 0)));
                
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (selecting) {
                            toggle(panel.getYIndex(), panel.getXIndex());
                        }
                    }
                });
                
                contentPane.add(panel);
                panelRow.add(panel);
                x += increment;
            }
            y += increment;
        }
    }
    
    /**
     * Clears the display of selected vehicles and the found paths.
     */
    public void clear() {
        //There might be some better way of iterating over values without modifying list other than copying everything
        Set<List<Integer>> copy = new HashSet<>();
        for (List<Integer> coordinate: selected) {
            copy.add(coordinate);
        }
        for (List<Integer> coordinate: copy) {
            toggle(coordinate.get(0), coordinate.get(1));
        }
        
        if (coordinator.isDone()) {
            for (List<Integer> coordinate: coordinator.getPath()) { //reset color of found path
                panelGrid.get(coordinate.get(0)).get(coordinate.get(1)).setBackground(colorGrid.get(coordinate.get(0)).get(coordinate.get(1)));
            }
        }
    }
    
    /**
     * Adds coordinate to selected set if not present; and removes it if present
     * Adjusts display of selected vehicles
     * 
     * @param y
     * @param x
     */
    public void toggle(int y, int x) {
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(y);
        coordinates.add(x);
        
        if (selected.contains(coordinates)) {
            selected.remove(coordinates);
            panelGrid.get(y).get(x).setBorder(new LineBorder(new Color(0, 0, 0)));
        }
        else {
            selected.add(coordinates);
            panelGrid.get(y).get(x).setBorder(new LineBorder(new Color(255, 255, 255)));
        }
    }
    
    public void timeStep() {
        //HACKY SPEEDUP FOR NOT HAVING TO CLICK TOP LEFT EVERY TIME
        if (selected.size() == 0) {
            toggle(0, 0);
            coordinator = new Controller(selected, depthGrid);
        }
        ////////////////////////////////////////////////
        
        
        
        //do nothing and stop playing if in an invalid state
        if (done || selecting || selected.size() == 0) {
            if (playing) {
                playButton.doClick();
            }
            return;
        }
        
        if (coordinator.isDone()) {
            for (List<Integer> coordinate: coordinator.getTraversedPath()) {
                panelGrid.get(coordinate.get(0)).get(coordinate.get(1)).setBackground(new Color(0, 0, 0));
            }
            for (List<Integer> coordinate: coordinator.getPath()) {
                //colors everything on found path magenta
//                panelGrid.get(coordinate.get(0)).get(coordinate.get(1)).setBorder(new LineBorder(new Color(0, 0, 0)));
                panelGrid.get(coordinate.get(0)).get(coordinate.get(1)).setBackground(new Color(255, 255, 255));
            }

            
            if (playing) {//stop playing when done
                playButton.doClick();                
            }
            
            done = true;
        }
        else {
            clear();
            for (List<Integer> coordinate: coordinator.newPositions()) {
                toggle(coordinate.get(0), coordinate.get(1));
            }
        }
    }
    
    public static void main(String[] args) {
    	System.out.println(System.getProperty("user.dir"));
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {       
                String filename = "";
//                filename = "/Users/fred/Desktop/UROP/channelSearch/Bathymetry/Straight Path.txt";
//                filename = "/Users/fred/Desktop/UROP/channelSearch/Bathymetry/Bent Path.txt";
                filename = "/Users/fred/Desktop/UROP/channelSearch/Bathymetry/Curved Path.txt";
//                filename = "/Users/fred/Desktop/UROP/channelSearch/Bathymetry/Y Path.txt";


                    try {
                        FileReader reader = new FileReader(filename);
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        
                        String line = bufferedReader.readLine();
                        String dimensions[] = line.split("x");
                        
                        if (dimensions.length != 2) {
                            System.out.println("ERROR");
                            reader.close();
                            bufferedReader.close();
                            return;
                        }
                        
                        int rowIndex = 0;
                        List<List<Color>> colorMatrix = new ArrayList<>();
                        List<List<Integer>> depthMatrix = new ArrayList<>();

                        while ((line = bufferedReader.readLine()) != null) {
                            List<Color> colorRow = new ArrayList<>();
                            List<Integer> depthRow = new ArrayList<>();
                            for (int i = 0; i < line.length(); i++) {
                                int depth = Integer.valueOf(line.substring(i, i+1));
                                Color color = depthToColor.get(depth);
                                
                                depthRow.add(depth);
                                colorRow.add(color);
                            }
                            
                            depthMatrix.add(depthRow);
                            colorMatrix.add(colorRow);
                        }
                        
                        reader.close();
                        bufferedReader.close();
                        
                        
                        
                        List<List<Integer>> dtranspose = new ArrayList<>();
                        for (int i = 0; i < depthMatrix.get(0).size(); i++) {
                            List<Integer> column = new ArrayList<>();
                            for (int j = 0; j < depthMatrix.size(); j++) {
                                column.add(depthMatrix.get(j).get(i));
                            }
                            dtranspose.add(column);
                        }
                        
                        List<List<Color>> ctranspose = new ArrayList<>();
                        for (int i = 0; i < colorMatrix.get(0).size(); i++) {
                            List<Color> column = new ArrayList<>();
                            for (int j = 0; j < colorMatrix.size(); j++) {
                                column.add(colorMatrix.get(j).get(i));
                            }
                            ctranspose.add(column);
                        }
                        
                        
//                        Simulation gui = new Simulation(depthMatrix, colorMatrix);
                        Simulation gui = new Simulation(dtranspose, ctranspose);

                        gui.setVisible(true);
                    }
                    catch (IOException exception) {
                        System.out.println("error here");
                    }
                }
        });
    }
}
