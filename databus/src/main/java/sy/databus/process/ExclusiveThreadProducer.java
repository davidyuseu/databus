package sy.databus.process;

/**
 * 标记接口，由独占线程的生产者继承
 * 被标记的子类会被分配{@link sy.databus.organize.ProcessorManager#exclusiveExecutors}独占执行器
 * */
public interface ExclusiveThreadProducer {
}
