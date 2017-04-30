package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.MovementDetail;

import javax.ejb.ApplicationException;

/**
 * @author
 * @version 2.0
 */

@ApplicationException(rollback = true)
public class MovementDetailException extends Exception {
    private MovementDetail movementDetail;

    public MovementDetailException(MovementDetail movementDetail) {
        this.movementDetail = movementDetail;
    }

    public MovementDetailException(String message, MovementDetail movementDetail) {
        super(message);
        this.movementDetail = movementDetail;
    }

    public MovementDetailException(String message, Throwable cause, MovementDetail movementDetail) {
        super(message, cause);
        this.movementDetail = movementDetail;
    }

    public MovementDetailException(Throwable cause, MovementDetail movementDetail) {
        super(cause);
        this.movementDetail = movementDetail;
    }

    public MovementDetail getMovementDetail() {
        return movementDetail;
    }

    public void setMovementDetail(MovementDetail movementDetail) {
        this.movementDetail = movementDetail;
    }
}
