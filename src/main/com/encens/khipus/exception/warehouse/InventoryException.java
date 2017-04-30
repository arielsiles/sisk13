package com.encens.khipus.exception.warehouse;

import com.encens.khipus.util.warehouse.InventoryMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.0
 */

public class InventoryException extends Exception {
    private List<InventoryMessage> inventoryMessages = new ArrayList<InventoryMessage>();

    public InventoryException(List<InventoryMessage> inventoryMessages) {
        this.inventoryMessages = inventoryMessages;
    }

    public InventoryException(String message, List<InventoryMessage> inventoryMessages) {
        super(message);
        this.inventoryMessages = inventoryMessages;
    }

    public InventoryException(String message, Throwable cause, List<InventoryMessage> inventoryMessages) {
        super(message, cause);
        this.inventoryMessages = inventoryMessages;
    }

    public InventoryException(Throwable cause, List<InventoryMessage> inventoryMessages) {
        super(cause);
        this.inventoryMessages = inventoryMessages;
    }

    public List<InventoryMessage> getInventoryMessages() {
        return inventoryMessages;
    }
}
