package sy.databus.global;

public enum ProcessorType {
    FRAME((byte) 0x01, Category.PROCESSOR_MPSC_COMPUTING),

    UDP_MULTICAST((byte) 0x02, Category.PROCESSOR_NIO),
    LSN_REPLAY_FILE_READER((byte) 0x06, Category.PROCESSOR_SINGLE_EXECUTOR),
    KWH_REPLAY_FILE_READER((byte) 0x07, Category.PROCESSOR_SINGLE_EXECUTOR),
    ZYF_REPLAY_FILE_READER((byte) 0x08, Category.PROCESSOR_SINGLE_EXECUTOR),
    ORDINARY_FILE_READER((byte) 0x09, Category.PROCESSOR_SINGLE_EXECUTOR),
    PARAMS_RECORDER((byte) 0x0a, Category.PROCESSOR_MPSC_IO),
    DM_DATA_WRITER((byte) 0x0b, Category.PROCESSOR_SINGLE_EXECUTOR),
    FILE_DATA_WRITER((byte) 0x0c, Category.PROCESSOR_MPSC_IO);

    private final byte code;
    private final Category category;

    ProcessorType(byte code, Category category) {
        this.code = code;
        this.category = category;
    }

    public byte getCode(){return code;}

    public Category getCategory() {
        return category;
    }

    public static ProcessorType getValue(byte code){
        for (ProcessorType type : ProcessorType.values()){
            if (type.getCode() == code)
                return type;
        }
        return null;
    }

    public enum Category {
        PROCESSOR_SINGLE_EXECUTOR("Single Executor"),
        PROCESSOR_NIO("NIO Processor"),
        PROCESSOR_MPSC_COMPUTING("MPSC Computing Processor"),
        PROCESSOR_MPSC_IO("MPSC IO Processor");

        String name;

        Category(String name) {
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

}
