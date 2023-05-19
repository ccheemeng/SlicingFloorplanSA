import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double p = sc.nextDouble();
        double epsilon = sc.nextDouble();
        double r = sc.nextDouble();
        int k = sc.nextInt();
        int plotWidth = sc.nextInt();
        int plotHeight = sc.nextInt();
        double aspectRatioInfluence = sc.nextDouble();
        int numOfRooms = sc.nextInt();
        ImList<Integer> widths = new ImList<Integer>();
        ImList<Integer> heights = new ImList<Integer>();
        for (int i = 0; i < numOfRooms; i++) {
            try {
                widths = widths.add(sc.nextInt());
                heights = heights.add(sc.nextInt());
            }
            catch (NoSuchElementException e) {
                System.out.println("Not enough dimensions supplied for number of rooms. Terminating.");
                return;
            }
        }
        Simulator simulator = new Simulator(p, epsilon, r, k,
                plotWidth, plotHeight, aspectRatioInfluence, widths, heights);
        String output = simulator.simulate();
        System.out.println(output);
        try {
            File file = new File("output.txt");
            FileWriter fileWriter = new FileWriter(file);
            file.createNewFile();
            fileWriter.write(output);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error! Could not write output");
        }
        sc.close();
    }
}
