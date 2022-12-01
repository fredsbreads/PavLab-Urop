package channelSearch;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

public class BathymetryInterface extends JFrame {

    private JPanel contentPane;
    private Color selectedColor = Color.CYAN;
    
    private JFrame window = this;
    
    private List<List<JPanel>> panelGrid = new ArrayList<>();
    
    private Map<Color, Integer> colorToDepth = Map.of(Color.cyan, 1, Color.green, 2, Color.yellow, 3, Color.orange, 4, Color.red, 5);
    private Map<Integer, Color> depthToColor = Map.of(1, Color.cyan, 2, Color.green, 3, Color.yellow, 4, Color.orange, 5, Color.red);

    
    public BathymetryInterface(int length, int height, String filename) {
       this(length, height);
       
       try {
           FileReader reader = new FileReader(filename);
           BufferedReader bufferedReader = new BufferedReader(reader);
           
           String line = bufferedReader.readLine(); //ignore dimensions
           int row = 0;
           while ((line = bufferedReader.readLine()) != null) {
               for (int i = 0; i < line.length(); i++) {
                   int depth = Integer.valueOf(line.substring(i, i+1));
                   Color color = depthToColor.get(depth);
 
                   panelGrid.get(row).get(i).setBackground(color);
               }
               row++;
           }
           
           reader.close();
           bufferedReader.close();
       }
       catch (IOException exception) {
           exception.printStackTrace();               
       }
    }
    
    public BathymetryInterface(int length, int height) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 1600, 900);

        contentPane = new JPanel();
        contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JButton button1 = new JButton("1");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = Color.cyan;
            }
        });
        button1.setForeground(Color.CYAN);
        button1.setBounds(6, 6, 117, 29);
        contentPane.add(button1);

        JButton button2 = new JButton("2");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = Color.green;
            }
        });
        button2.setForeground(Color.GREEN);
        button2.setBounds(135, 6, 117, 29);
        contentPane.add(button2);

        JButton button3 = new JButton("3");
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = Color.yellow;
            }
        });
        button3.setForeground(Color.YELLOW);
        button3.setBounds(264, 6, 117, 29);
        contentPane.add(button3);

        JButton button4 = new JButton("4");
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = Color.orange;
            }
        });
        button4.setForeground(Color.ORANGE);
        button4.setBounds(393, 6, 117, 29);
        contentPane.add(button4);

        JButton button5 = new JButton("5");
        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = Color.red;
            }
        });
        button5.setForeground(Color.RED);
        button5.setBounds(522, 6, 117, 29);
        contentPane.add(button5);

        JButton saveButton = new JButton("Save Text File");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat formatter= new SimpleDateFormat("'Bathymetry' yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String filename = System.getProperty("user.dir") + File.separator + "Bathymetry" + File.separator + formatter.format(date) + ".txt";
                
                System.out.println(filename);
                
                try {
                    FileWriter writer = new FileWriter(filename);
                    writer.write(String.valueOf(length) + "x" + String.valueOf(height));
                    for (List<JPanel> row: panelGrid) {
                        writer.write(System.lineSeparator());

                        String line = "";
                        for (JPanel panel: row) {
                            line += String.valueOf(colorToDepth.get(panel.getBackground()));
                        }
                        
                        writer.write(line);
                    }
                    
                    writer.close();
//                    System.out.println("made it");
                }
                catch (IOException exception) {
                    JOptionPane.showMessageDialog(window, "Error while saving file.");
                    exception.printStackTrace();
                }



            }
        });
        saveButton.setBounds(705, 6, 117, 29);
        contentPane.add(saveButton);

        // buttons currently end at 35 (9 + 25) so start grid at 40
        // 40 -> 840 = 800 y space, 1500 x space (50 -> 1550)

        if (length < 1 || height < 1) {
            return;
        }

        int y = 45;
        int x = 25;

        int yIncrement = 800 / height;
        int xIncrement = 1400 / length;

        for (int j = 0; j < height; j++) {
            List<JPanel> row = new ArrayList<>();
            panelGrid.add(row);
            for (int i = 0; i < length; i++) {
                JPanel panel = new JPanel();
                
//                if (i == 0 || i == length - 1 || j == 0 || j == height - 1) {
//                    panel.setBackground(Color.green);
//                }
//                else {
//                    panel.setBackground(Color.cyan);
//                }
                panel.setBackground(Color.cyan);
                
//                panel.setBounds(x, y, xIncrement, yIncrement);
//                panel.setBounds(x, y, xIncrement, xIncrement);
                panel.setBounds(x, y, yIncrement, yIncrement);


                panel.setBorder(new LineBorder(new Color(0, 0, 0)));
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        panel.setBackground(selectedColor);
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) { //click held down
                            panel.setBackground(selectedColor);
                        }
                    }
                });
                contentPane.add(panel);

//                x += xIncrement;
                x += yIncrement;

                
                row.add(panel);
            }

            y += yIncrement;
//            y += xIncrement;
            x = 25;
        }

    }

    public static void main (String[] args) {
        System.out.println(System.getProperty("user.dir"));


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {       
                String filename = "";
//                filename = "/Users/fred/eclipse-workspace/Urop/Bathymetry/Straight Path.txt";
//                filename = "/Users/fred/eclipse-workspace/Urop/Bathymetry/Bent Path.txt";
//                filename = "/Users/fred/eclipse-workspace/Urop/Bathymetry/Curved Path.txt";
//                filename = "/Users/fred/eclipse-workspace/Urop/Bathymetry/Y Path.txt";


                
                if (filename.isEmpty()) {
                    BathymetryInterface gui = new BathymetryInterface(75, 50);
                    gui.setVisible(true);
                }
                else {
                    try {
                        FileReader reader = new FileReader(filename);
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        
                        String line = bufferedReader.readLine();
                        String dimensions[] = line.split("x");
                        
                        if (dimensions.length != 2) {
                            System.out.println("ERROR");
                        }
                        
                        BathymetryInterface gui = new BathymetryInterface(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]), filename);
                        
                        gui.setVisible(true);
                        
                        reader.close();
                        bufferedReader.close();
        
                    }
                    catch (IOException exception) {
                        System.out.println("error here");
                    }
                }
            }
        });
    }
}
