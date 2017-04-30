package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;


/**
 * @author
 * @version 3.4
 */

@Stateless
@Name("dismissalDetailService")
@AutoCreate
public class DismissalDetailServiceBean extends GenericServiceBean implements DismissalDetailService {

}