import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SimpleImageProcessor extends JFrame {
    private BufferedImage image;
    private JLabel imageDisplay;
    private JLabel statusText;
    private JButton loadButton, processButton;
    private JTextField threadInput;
    
    public SimpleImageProcessor() {
        // Setup window
        setTitle("Simple Multi-threaded Image Processor");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create top panel with controls
        JPanel controls = new JPanel();
        controls.add(new JLabel("Threads:"));
        threadInput = new JTextField("2", 3);
        controls.add(threadInput);
        loadButton = new JButton("Load Image");
        processButton = new JButton("Make Grayscale");
        processButton.setEnabled(false);
        controls.add(loadButton);
        controls.add(processButton);
        
        // Create center area for image
        imageDisplay = new JLabel("No image loaded", SwingConstants.CENTER);
        imageDisplay.setBorder(BorderFactory.createEtchedBorder());
        
        // Create bottom status bar
        statusText = new JLabel("Ready");
        
        // Add everything to window
        add(controls, BorderLayout.NORTH);
        add(imageDisplay, BorderLayout.CENTER);
        add(statusText, BorderLayout.SOUTH);
        
        // Setup button actions
        loadButton.addActionListener(e -> loadImage());
        processButton.addActionListener(e -> processImage());
        
        setLocationRelativeTo(null);
    }
    
    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                image = ImageIO.read(file);
                
                // Scale image to fit window
                ImageIcon icon = new ImageIcon(image.getScaledInstance(400, 300, Image.SCALE_SMOOTH));
                imageDisplay.setIcon(icon);
                imageDisplay.setText("");
                
                statusText.setText("Image loaded: " + image.getWidth() + " x " + image.getHeight());
                processButton.setEnabled(true);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot load image!");
            }
        }
    }
    
    private void processImage() {
        if (image == null) return;
        
        // Get number of threads
        int numThreads = Integer.parseInt(threadInput.getText());
        statusText.setText("Processing with " + numThreads + " threads...");
        processButton.setEnabled(false);
        
        long startTime = System.currentTimeMillis();
        
        // Split image into strips for each thread
        int height = image.getHeight();
        int rowsPerThread = height / numThreads;
        
        // Create worker threads
        Thread[] workers = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            int startRow = i * rowsPerThread;
            int endRow = (i == numThreads - 1) ? height : startRow + rowsPerThread;
            
            workers[i] = new Thread(() -> convertToGray(startRow, endRow));
            workers[i].start();
        }
        
        // Wait for all threads to finish
        new Thread(() -> {
            try {
                for (Thread worker : workers) {
                    worker.join();
                }
                
                long endTime = System.currentTimeMillis();
                
                // Update display on main thread
                SwingUtilities.invokeLater(() -> {
                    ImageIcon newIcon = new ImageIcon(image.getScaledInstance(400, 300, Image.SCALE_SMOOTH));
                    imageDisplay.setIcon(newIcon);
                    statusText.setText("Done! Time: " + (endTime - startTime) + "ms");
                    processButton.setEnabled(true);
                });
                
            } catch (InterruptedException e) {
                statusText.setText("Processing interrupted");
            }
        }).start();
    }
    
    // Convert part of image to grayscale
    private void convertToGray(int startRow, int endRow) {
        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // Get pixel color
                int pixel = image.getRGB(x, y);
                
                // Extract red, green, blue
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;
                
                // Calculate gray value
                int gray = (red + green + blue) / 3;
                
                // Create new gray pixel
                int grayPixel = (gray << 16) | (gray << 8) | gray;
                
                // Set new pixel
                image.setRGB(x, y, grayPixel);
            }
        }
    }
    
    public static void main(String[] args) {
        new SimpleImageProcessor().setVisible(true);
    }
}