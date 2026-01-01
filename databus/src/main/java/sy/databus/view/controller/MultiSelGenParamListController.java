package sy.databus.view.controller;

import javafx.scene.control.TextField;
import lombok.Getter;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.AbstractMultiSelectObList;
import sy.databus.entity.property.MultiSelGenParamList;
import sy.databus.process.Console;

import java.lang.reflect.Field;

/**
 * entity: {@link MultiSelGenParamList}
 * */
public class MultiSelGenParamListController extends MultiSelObListController implements MSObListControl{

    @Getter
    private TextField tfFrameLen = new TextField();

    public static MultiSelGenParamListController buildController(Console console,
                                                                 Field field,
                                                                 Object obj,
                                                                 MultiSelGenParamList mulSelObList) {
        MultiSelGenParamListController cachedController
                = (MultiSelGenParamListController) CacheFactory.getWeakCache(MultiSelGenParamListController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, mulSelObList);
            return cachedController;
        } else {
            return new MultiSelGenParamListController(console, field, obj, mulSelObList);
        }
    }

    public MultiSelGenParamListController(Console console, Field field, Object parentObj, MultiSelGenParamList mulSelObList) {
        super(console, field, parentObj, mulSelObList);
        tfFrameLen.setMinHeight(23d);
        tfFrameLen.setPrefSize(44d, 23d);
        tfFrameLen.setText(String.valueOf(mulSelObList.getLimitedFrameLen()));
        tfFrameLen.focusedProperty().addListener((ob, oldVar, newVar) -> {
            if (!newVar) {
                if (this.multiSelectObList instanceof MultiSelGenParamList multiSelGenParamList) {
                    if(tfFrameLen.getText() == null || tfFrameLen.getText().equals("")
                            || !tfFrameLen.getText().matches("^\\d+$")) {
                        tfFrameLen.setText(String.valueOf(multiSelGenParamList.getLimitedFrameLen()));
                        return;
                    }
                    int frameLen = Integer.parseInt(tfFrameLen.getText());
                    int oldFrameLen = multiSelGenParamList.getLimitedFrameLen();
                    multiSelGenParamList.setLimitedFrameLen(frameLen);
                    multiSelGenParamList.limitedByFrameLen();
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
        if (mulSelObList instanceof MultiSelGenParamList multiSelGenParamList) {
            tfFrameLen.setText(String.valueOf(multiSelGenParamList.getLimitedFrameLen()));
        }
    }
}