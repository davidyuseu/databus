package sy.databus.view.controller;

import javafx.scene.control.TextField;
import lombok.Getter;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.AbstractMultiSelectObList;
import sy.databus.entity.property.MultiSelNumParamList;
import sy.databus.process.Console;

import java.lang.reflect.Field;

/**
 * entity: {@link MultiSelNumParamList}
 * */
public class MultiSelNumParamListController extends MultiSelObListController implements MSObListControl{

    @Getter
    private TextField tfFrameLen = new TextField();

    public static MultiSelNumParamListController buildController(Console console,
                                                           Field field,
                                                           Object obj,
                                                                 MultiSelNumParamList mulSelObList) {
        MultiSelNumParamListController cachedController
                = (MultiSelNumParamListController) CacheFactory.getWeakCache(MultiSelNumParamListController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, mulSelObList);
            return cachedController;
        } else {
            return new MultiSelNumParamListController(console, field, obj, mulSelObList);
        }
    }

    public MultiSelNumParamListController(Console console, Field field, Object parentObj, MultiSelNumParamList mulSelObList) {
        super(console, field, parentObj, mulSelObList);
        tfFrameLen.setMinHeight(23d);
        tfFrameLen.setPrefSize(44d, 23d);
        tfFrameLen.setText(String.valueOf(mulSelObList.getLimitedFrameLen()));
        tfFrameLen.focusedProperty().addListener((ob, oldVar, newVar) -> {
            if (!newVar) {
                if (this.multiSelectObList instanceof MultiSelNumParamList multiSelNumParamList) {
                    if(tfFrameLen.getText() == null || tfFrameLen.getText().equals("")
                            || !tfFrameLen.getText().matches("^\\d+$")) {
                        tfFrameLen.setText(String.valueOf(multiSelNumParamList.getLimitedFrameLen()));
                        return;
                    }
                    int frameLen = Integer.parseInt(tfFrameLen.getText());
                    int oldFrameLen = multiSelNumParamList.getLimitedFrameLen();
                    multiSelNumParamList.setLimitedFrameLen(frameLen);
                    multiSelNumParamList.limitedByFrameLen();
                    if (frameLen != oldFrameLen)
                        listView.refresh();
                }
            }
        });
        selBtnPane.getChildren().addAll(tfFrameLen);
    }

    @Override
    public void rebuild(Console console, Field field, Object parentObj, AbstractMultiSelectObList mulSelObList) {
        super.rebuild(console, field, parentObj, mulSelObList);
        if (mulSelObList instanceof MultiSelNumParamList multiSelNumParamList) {
            tfFrameLen.setText(String.valueOf(multiSelNumParamList.getLimitedFrameLen()));
        }
    }
}
