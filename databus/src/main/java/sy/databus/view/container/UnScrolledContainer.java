package sy.databus.view.container;

public class UnScrolledContainer extends Container{
    public static final String STYLE_CLASS_OUTER_NON_SCROLLED ="outer-nonscrolled-titled";

    public UnScrolledContainer(String title) {
        super(title);

        this.getStyleClass().add(STYLE_CLASS_OUTER_NON_SCROLLED);
    }
}
