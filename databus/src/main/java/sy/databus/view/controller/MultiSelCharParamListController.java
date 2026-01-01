package sy.databus.view.controller;


import javafx.scene.control.TextField;
import lombok.Getter;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.AbstractMultiSelectObList;
import sy.databus.entity.property.MultiSelCharParamList;
import sy.databus.process.Console;

import java.lang.reflect.Field;

/**
 * entity: {@link MultiSelCharParamList}
 * */
public class MultiSelCharParamListController extends MultiSelObListController implements MSObListControl {

    @Getter
    private TextField tfFrameLen = new TextField();

    public static MultiSelCharParamListController buildController(Console console,
                                                                  Field field,
                                                                  Object obj,
                                                                  MultiSelCharParamList mulSelObList) {
        MultiSelCharParamListController cachedController
                = (MultiSelCharParamListController) CacheFactory.getWeakCache(MultiSelCharParamListController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, mulSelObList);
            return cachedController;
        } else {
            return new MultiSelCharParamListController(console, field, obj, mulSelObList);
        }
    }

    public MultiSelCharParamListController(Console console, Field field, Object parentObj, MultiSelCharParamList mulSelObList) {
        super(console, field, parentObj, mulSelObList);
        tfFrameLen.setMinHeight(23d);
        tfFrameLen.setPrefSize(44d, 23d);
        tfFrameLen.setText(String.valueOf(mulSelObList.getLimitedFrameLen()));
        tfFrameLen.focusedProperty().addListener((ob, oldVar, newVar) -> {
            if (!newVar) {
                if (this.multiSelectObList instanceof MultiSelCharParamList multiSelCharParamList) {
                    if(tfFrameLen.getText() == null || tfFrameLen.getText().equals("")
                            || !tfFrameLen.getText().matches("^\\d+$")) {
                        tfFrameLen.setText(String.valueOf(multiSelCharParamList.getLimitedFrameLen()));
                        return;
                    }
                    int frameLen = Integer.parseInt(tfFrameLen.getText());
                    int oldFrameLen = multiSelCharParamList.getLimitedFrameLen();
                    multiSelCharParamList.setLimitedFrameLen(frameLen);
                    multiSelCharParamList.limitedByFrameLen();
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
        if (mulSelObList instanceof MultiSelCharParamList multiSelCharParamList) {
            tfFrameLen.setText(String.valueOf(multiSelCharParamList.getLimitedFrameLen()));
        }

    }
}