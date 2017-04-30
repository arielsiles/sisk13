package com.encens.khipus.model;

import java.io.Serializable;

/**
 * Base model interface to be used to indetify any entity which implements this model
 *
 * @author
 * @version $Id: BaseModel.java 2008-8-20 17:53:59 $
 */
public interface BaseModel extends Serializable {
    Object getId();
}
