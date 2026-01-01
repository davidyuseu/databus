package excel.write.handler.impl;

import excel.write.handler.RowWriteHandler;
import excel.write.handler.context.RowWriteHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Default row handler.
 *
 * @author Jiaju Zhuang
 */
@Slf4j
public class DefaultRowWriteHandler implements RowWriteHandler {

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        context.getWriteSheetHolder().setLastRowIndex(context.getRowIndex());
    }

}
