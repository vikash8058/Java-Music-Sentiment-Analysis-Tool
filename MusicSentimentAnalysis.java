import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.AudioEvent;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.converter.Converter;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.*;

public class MusicSentimentAnalysis {
    public static void main(String[] args) {
        // Step 1: Select an MP3 file
        File selectedFile = selectMP3File();
        if (selectedFile == null) {
            System.out.println(" No file selected. Exiting...");
            return;
        }

        // Step 2: Convert MP3 to WAV
        String wavFilePath = convertMP3toWAV(selectedFile);
        if (wavFilePath == null) {
            System.out.println(" MP3 conversion failed. Exiting...");
            return;
        }

        // Step 3: Process the WAV file
        AudioProcessor processor = new AudioProcessor(wavFilePath);
        double pitch = processor.extractPitch();
        double tempo = processor.extractTempo();
        double loudness = processor.extractLoudness();

        // Step 4: Analyze Mood
        String mood = SentimentAnalyzer.analyzeMood(pitch, tempo, loudness);
        System.out.println("🎵 Detected Mood: " + mood);
    }


    private static File selectMP3File() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an MP3 File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 Files", "mp3"));

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }


     //   Converts an MP3 file to a WAV file.

    private static String convertMP3toWAV(File mp3File) {
        try {
            String wavPath = mp3File.getAbsolutePath().replace(".mp3", ".wav");
            new Converter().convert(mp3File.getAbsolutePath(), wavPath);
            return wavPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


 // Processes an audio file for pitch, tempo, and loudness extraction.

class AudioProcessor {
    private String filePath;

    public AudioProcessor(String filePath) {
        this.filePath = filePath;
    }

    public double extractPitch() {
        final float[] pitchResult = {0};
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                throw new IOException(" Error: File not found -> " + filePath);
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioInputStream.getFormat();

            int bufferSize = 2048;
            int overlap = bufferSize / 2;

            if (bufferSize <= 0 || overlap < 0) {
                throw new IllegalArgumentException(" Error: Invalid buffer size or overlap.");
            }

            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);

            dispatcher.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    format.getSampleRate(), bufferSize,
                    (PitchDetectionResult result, AudioEvent audioEvent) -> {
                        if (result.getPitch() != -1) {
                            pitchResult[0] = result.getPitch();
                        }
                    }
            ));
            dispatcher.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pitchResult[0];
    }

    public double extractTempo() {
        return Math.random() * 150; // Placeholder for actual tempo extraction
    }

    public double extractLoudness() {
        return Math.random() * 100; // Placeholder for actual loudness extraction
    }
}


class SentimentAnalyzer {
    public static String analyzeMood(double pitch, double tempo, double loudness) {
        if (tempo > 120 && pitch > 200) return "Happy";
        else if (tempo < 80 && pitch < 150) return "Sad";
        else return "Calm";
    }
}
