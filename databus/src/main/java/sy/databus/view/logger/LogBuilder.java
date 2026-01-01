package sy.databus.view.logger;

import lombok.Getter;

public class LogBuilder {

    private final int maxCountLimit;

    private final StringBuilder builder = new StringBuilder();

    @Getter
    private int appendingCount = 0;

    public LogBuilder(int maxCountLimit) {
        this.maxCountLimit = maxCountLimit;

    }

    public void addLine(String line) {
        if (!line.endsWith(System.lineSeparator()))
            builder.insert(0, System.lineSeparator());
        builder.insert(0, line);
        appendingCount ++;
    }

    public boolean addLineIfNotMax(String line) {
        if (appendingCount > maxCountLimit)
            return false;

        addLine(line);
        return true;
    }

    public void reset() {
        builder.setLength(0);
        appendingCount = 0;
    }

    public String takeString() {
        appendingCount = 0;
        String text = builder.toString();
        builder.setLength(0);
        return text;
    }

    public boolean reachedMaxLength() {
        return appendingCount > maxCountLimit;
    }


}
