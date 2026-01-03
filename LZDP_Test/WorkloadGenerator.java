import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.io.*;

public class DataFlowGenerator {
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_INTERVAL_MS = 1000;
    
    private final List<DataProcessor> processors = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int intervalMs = DEFAULT_INTERVAL_MS;
    
    public interface DataProcessor {
        void process(List<DataRecord> records);
    }
    
    public static class DataRecord {
        private final long timestamp;
        private final Map<String, Object> data;
        
        public DataRecord(long timestamp, Map<String, Object> data) {
            this.timestamp = timestamp;
            this.data = new HashMap<>(data);
        }
        
        public long getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return new HashMap<>(data); }
        
        @Override
        public String toString() {
            return String.format("DataRecord{timestamp=%d, data=%s}", timestamp, data);
        }
    }
    
    public static class DataGenerator {
        private final Random random = new Random();
        private final String[] names = {"Alice", "Bob", "Charlie", "David", "Eve"};
        private final String[] actions = {"login", "logout", "purchase", "view", "search"};
        
        public DataRecord generateRecord() {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", random.nextInt(10000));
            data.put("name", names[random.nextInt(names.length)]);
            data.put("action", actions[random.nextInt(actions.length)]);
            data.put("value", random.nextDouble() * 1000);
            data.put("category", "category_" + (random.nextInt(10) + 1));
            
            return new DataRecord(System.currentTimeMillis(), data);
        }
    }
    
    public void setBatchSize(int batchSize) {
        if (batchSize <= 0) throw new IllegalArgumentException("Batch size must be positive");
        this.batchSize = batchSize;
    }
    
    public void setInterval(int intervalMs) {
        if (intervalMs <= 0) throw new IllegalArgumentException("Interval must be positive");
        this.intervalMs = intervalMs;
    }
    
    public void addProcessor(DataProcessor processor) {
        processors.add(Objects.requireNonNull(processor));
    }
    
    public void start() {
        if (running) throw new IllegalStateException("Generator already running");
        
        running = true;
        scheduler = Executors.newScheduledThreadPool(2);
        DataGenerator generator = new DataGenerator();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<DataRecord> batch = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize; i++) {
                    batch.add(generator.generateRecord());
                }
                
                for (DataProcessor processor : processors) {
                    try {
                        processor.process(new ArrayList<>(batch));
                    } catch (Exception e) {
                        System.err.println("Processor error: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Batch generation error: " + e.getMessage());
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
        
        System.out.println("Data flow generator started with batch size " + batchSize + 
                          " and interval " + intervalMs + "ms");
    }
    
    public void stop() {
        if (!running) return;
        
        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Data flow generator stopped");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public static void main(String[] args) throws InterruptedException {
        DataFlowGenerator generator = new DataFlowGenerator();
        
        // Configration
        generator.setBatchSize(100);
        generator.setInterval(500);
        
        // Add processor - Control output
        generator.addProcessor(records -> {
            System.out.println("=== Batch Processed ===");
            System.out.println("Record count: " + records.size());
            records.forEach(System.out::println);
            System.out.println("======================\n");
        });
        
        // Add processor - File storage
        generator.addProcessor(new FileDataProcessor("data_flow_output.txt"));
        
        // Add processor - Statistics analysis
        generator.addProcessor(new StatisticsProcessor());
        
        // Start generator
        generator.start();
        
        // Stop after running 30s
        Thread.sleep(30000);
        generator.stop();
    }
    
    static class FileDataProcessor implements DataProcessor {
        private final String filename;
        private final Object lock = new Object();
        
        public FileDataProcessor(String filename) {
            this.filename = filename;
        }
        
        @Override
        public void process(List<DataRecord> records) {
            synchronized (lock) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
                    writer.println("=== Batch at " + new Date() + " ===");
                    for (DataRecord record : records) {
                        writer.println(record);
                    }
                    writer.println();
                } catch (IOException e) {
                    System.err.println("File write error: " + e.getMessage());
                }
            }
        }
    }
    
    static class StatisticsProcessor implements DataProcessor {
        private final Map<String, Integer> actionCount = new ConcurrentHashMap<>();
        private final Map<String, Double> categorySum = new ConcurrentHashMap<>();
        private final AtomicLong totalRecords = new AtomicLong(0);
        
        @Override
        public void process(List<DataRecord> records) {
            totalRecords.addAndGet(records.size());
            
            for (DataRecord record : records) {
                Map<String, Object> data = record.getData();
                
                // Counting action type
                String action = (String) data.get("action");
                if (action != null) {
                    actionCount.merge(action, 1, Integer::sum);
                }
                
                // Computing total
                String category = (String) data.get("category");
                Double value = (Double) data.get("value");
                if (category != null && value != null) {
                    categorySum.merge(category, value, Double::sum);
                }
            }
            
            // Input staticstics informaton
            System.out.println("=== Statistics ===");
            System.out.println("Total records: " + totalRecords.get());
            System.out.println("Action distribution: " + actionCount);
            System.out.println("Category totals: " + categorySum);
            System.out.println("==================\n");
        }
    }
}
