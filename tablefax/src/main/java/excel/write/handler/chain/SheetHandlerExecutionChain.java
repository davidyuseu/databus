package excel.write.handler.chain;

import excel.write.handler.SheetWriteHandler;
import excel.write.handler.context.SheetWriteHandlerContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Execute the sheet handler chain
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class SheetHandlerExecutionChain {
    /**
     * next chain
     */
    private excel.write.handler.chain.SheetHandlerExecutionChain next;
    /**
     * handler
     */
    private SheetWriteHandler handler;

    public SheetHandlerExecutionChain(SheetWriteHandler handler) {
        this.handler = handler;
    }

    public void beforeSheetCreate(SheetWriteHandlerContext context) {
        this.handler.beforeSheetCreate(context);
        if (this.next != null) {
            this.next.beforeSheetCreate(context);
        }
    }

    public void afterSheetCreate(SheetWriteHandlerContext context) {
        this.handler.afterSheetCreate(context);
        if (this.next != null) {
            this.next.afterSheetCreate(context);
        }
    }
    public void addLast(SheetWriteHandler handler) {
        excel.write.handler.chain.SheetHandlerExecutionChain context = this;
        while (context.next != null) {
            context = context.next;
        }
        context.next = new excel.write.handler.chain.SheetHandlerExecutionChain(handler);
    }
}
