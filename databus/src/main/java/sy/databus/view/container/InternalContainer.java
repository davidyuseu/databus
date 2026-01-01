package sy.databus.view.container;

public class InternalContainer extends Container{
    public static final String STYLE_CLASS_INTERNAL = "internal-titled";

    public InternalContainer(String title) {
        super(title);
        this.getStyleClass().add(STYLE_CLASS_INTERNAL);
    }
}
