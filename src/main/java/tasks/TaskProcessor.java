package tasks;

public class TaskProcessor {

    StringBuilder sbOut;
    String processLabel;

    public TaskProcessor(StringBuilder sb, String label) {
        sbOut = sb;
        processLabel = label;
    }
}
