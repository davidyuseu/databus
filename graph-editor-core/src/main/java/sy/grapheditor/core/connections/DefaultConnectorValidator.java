/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.connections;

import sy.grapheditor.api.GConnectorValidator;
import sy.grapheditor.core.connectors.DefaultConnectorTypes;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;

/**
 * Default validation rules that determine which connectors can be connected to each other.
 */
public class DefaultConnectorValidator implements GConnectorValidator {

    @Override
    public boolean prevalidate(final GConnector source, final GConnector target) {
        if (source == null || target == null) {
            return false;
        } else return !source.equals(target);
    }

    @Override
    public boolean validate(final GConnector source, final GConnector target) {
        if (source.getType() == null || target.getType() == null) {
            return false;
        }else if(DefaultConnectorTypes.isInput(source.getType()) == DefaultConnectorTypes.isInput(target.getType())) {
            // 不允许input-input或output->output的情况
            return false;
        }/*else if (!source.getConnections().isEmpty() || !target.getConnections().isEmpty()) {
            return false;
        }*/else if (source.getParent().equals(target.getParent())) {
            return false;
        }

        for(GConnection gConnection : source.getConnections()){//同一对“进”“出”不可重复连接
            if(gConnection.getTarget().equals(target) || gConnection.getSource().equals(target))
                return false;
        }

        /**-AbstractIntegratedProcessor层面的连接校验*/
        GConnector inputConnector = DefaultConnectorTypes.isInput(source.getType()) ? source : target;
        GConnector outputConnector = inputConnector == source ? target : source;
        boolean inputValidationResult = ((Connectable) inputConnector.getParent().getAttachment(PROCESSOR))
                .validateAsInput(outputConnector.getParent());
        boolean outputValidationResult = ((Connectable) outputConnector.getParent().getAttachment(PROCESSOR))
                .validateAsOutput(inputConnector.getParent());
        return inputValidationResult && outputValidationResult;

    }
    /*

    @Override
    public boolean validate(final GConnector source, final GConnector target) {

        if (source.getType() == null || target.getType() == null) {
            return false;
        } else if (source.getParent().equals(target.getParent())) {//自己的output不能连接自己的input
            return false;
        } else if (source.getType().equals(target.getType())) {//“进”不可连“进”，“出”不可连“出”
            return false;
        } else if (source.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)
                && !source.getConnections().isEmpty()) {
            return false;
        } */
        /*else if (target.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)//一个“进”只能连一个“出”
                        && !target.getConnections().isEmpty()) {
                    return false;
                }*//*

        if(source.getType().equals(TreeSkinConstants.TREE_OUTPUT_CONNECTOR)) {//同一对“进”“出”不可重复连接
            for(GConnection gConnection : source.getConnections()){
                if(gConnection.getTarget().equals(target))
                    return false;
            }
        }

        return true;
    }
    */

    @Override
    public String createConnectionType(final GConnector source, final GConnector target) {
        return null;
    }

    @Override
    public String createJointType(final GConnector source, final GConnector target) {
        return null;
    }
}