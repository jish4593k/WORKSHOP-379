import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_imgcodecs.Imgcodecs;
import org.bytedeco.opencv.opencv_imgproc.Imgproc;
import org.bytedeco.opencv.opencv_face.Face;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class FaceRecognitionApp {

    private JFrame frame;
    private JLabel faceImageLabel;
    private JLabel resultLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FaceRecognitionApp().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        frame = new JFrame("Face Recognition App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        faceImageLabel = new JLabel();
        frame.add(faceImageLabel);

        JButton browseButton = new JButton("Browse Image");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseImage();
            }
        });
        frame.add(browseButton);

        resultLabel = new JLabel("");
        frame.add(resultLabel);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitApp();
            }
        });
        frame.add(quitButton);

        frame.setLayout(new FlowLayout());
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    private void browseImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String imagePath = selectedFile.getAbsolutePath();

            ImageIcon icon = new ImageIcon(imagePath);
            Image image = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);
            faceImageLabel.setIcon(scaledIcon);

            // Perform face recognition
            String recognitionResult = recognizeFace(imagePath);
            resultLabel.setText(recognitionResult);
        }
    }

    private String recognizeFace(String imagePath) {
        FaceRecognizer faceRecognizer = Face.createLBPHFaceRecognizer();
        faceRecognizer.load("known_face.jpg"); // Replace with your known face image

        Mat unknownImage = Imgcodecs.imread(imagePath);
        Imgproc.cvtColor(unknownImage, unknownImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(unknownImage, unknownImage, new Size(300, 300));

        MatOfRect faceDetections = new MatOfRect();
        faceRecognizer.detectMultiScale(unknownImage, faceDetections);

        if (faceDetections.toArray().length > 0) {
            Rect rect = faceDetections.toArray()[0];
            Mat detectedFace = new Mat(unknownImage, rect);

            Mat labels = new Mat();
            Mat distances = new Mat();
            faceRecognizer.predict(detectedFace, labels, distances);

            int label = (int) labels.get(0, 0)[0];
            double distance = distances.get(0, 0)[0];

            if (distance < 80) {
                return "Face Recognized: This is the known face.";
            } else {
                return "Unknown Face: This face is not recognized.";
            }
        } else {
            return "No face detected in the selected image.";
        }
    }

    private void quitApp() {
        frame.dispose();
    }
}
