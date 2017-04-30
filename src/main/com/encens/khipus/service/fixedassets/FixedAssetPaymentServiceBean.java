package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * Service implementation of FixedAssetPaymentService
 *
 * @author
 * @version 2.25
 */

@Name("fixedAssetPaymentService")
@Stateless
@AutoCreate
public class FixedAssetPaymentServiceBean extends GenericServiceBean implements FixedAssetPaymentService {

}
