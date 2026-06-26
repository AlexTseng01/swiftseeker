/* 
Main class
*/

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileTracerApp {
    public static void main(String[] args) {
        System.out.println("Testing...");

        BlockingQueue<Path> queue = new ArrayBlockingQueue<>(10000);
        IndexDatabase db = new IndexDatabase();
        Path originPath = Paths.get("C:\\Users\\alext\\OneDrive\\Documents");
        FileScanner fs = new FileScanner(originPath, queue);

        Thread producer = new Thread(fs);
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    Path file = queue.take();

                    if (file.getFileName() != null && file.getFileName().toString().equals("__DONE__")) {
                        System.out.println("Database is updated.");
                        break;
                    }

                    db.insert(file);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("Starting system scan...");
        producer.start();
        consumer.start();
    }
}