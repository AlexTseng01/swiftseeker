/* 
Main class
*/

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileTracerApp {
    public static void main(String[] args) {
        System.out.println("Testing...");

        BlockingQueue<Path> queue = new ArrayBlockingQueue<>(10000);
        IndexDatabase db = new IndexDatabase();
        Path originPath = Paths.get("C:\\Users\\alext\\OneDrive\\Documents");
        FileScanner fs = new FileScanner(originPath, queue);

        // 1 Producer thread
        Thread producer = new Thread(fs);

        // 4 Consumer threads
        List<Thread> consumers = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            Thread consumer = new Thread(() -> {
                try {
                    while (true) {
                        Path file = queue.take();
                        System.out.println("Visited " + file);

                        if (file.getFileName() != null && file.getFileName().toString().equals("__DONE__")) {
                            break;
                        }

                        db.insert(file);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            consumers.add(consumer);
            consumer.start();
        }

        System.out.println("Starting file scan...");
        producer.start();

        try {
            producer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (int i = 0; i < 4; i++) {
            try {
                queue.put(Paths.get("__DONE__"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (Thread t : consumers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Database is updated.");
    }
}