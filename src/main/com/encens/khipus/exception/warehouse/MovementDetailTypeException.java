package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.MovementDetailType;

public class MovementDetailTypeException extends Exception {
    MovementDetailType expectedMovementDetailType;

    public MovementDetailTypeException(MovementDetailType expectedMovementDetailType) {
        this.expectedMovementDetailType = expectedMovementDetailType;
    }

    public MovementDetailType getExpectedMovementDetailType() {
        return expectedMovementDetailType;
    }

    public void setExpectedMovementDetailType(MovementDetailType expectedMovementDetailType) {
        this.expectedMovementDetailType = expectedMovementDetailType;
    }
}