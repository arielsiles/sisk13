package com.encens.khipus.service.dashboard;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.dashboard.Widget;

import javax.ejb.Local;

/**
 * @author
 * @version 2.26
 */
@Local
public interface WidgetService extends GenericService {

    Widget findByXmlId(String xmlId);

    Widget loadWidget(String xmlId);
}
