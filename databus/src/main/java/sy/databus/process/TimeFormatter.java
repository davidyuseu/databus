package sy.databus.process;

@FunctionalInterface
public interface TimeFormatter {
    StringBuilder format(StringBuilder builder, long timestamp);
}
