package com.encens.khipus.action.dashboard;

import com.encens.khipus.action.AppIdentity;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2
 */
@Name("agileWidgetAction")
public class AgileWidgetAction implements Serializable {

    private static final String VIEW_PERMISSION = "VIEW";
    private Map<Integer, List<SemaphoreBehaviorWidgetAction>> rangeMeterWidgetActionMap = new HashMap<Integer, List<SemaphoreBehaviorWidgetAction>>();
    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;

    @Create
    public void initialize() {
        setup();
    }

    public void setup() {
        rangeMeterWidgetActionMap.put(SemaphoreBehaviorWidgetAction.firstPosition, new ArrayList<SemaphoreBehaviorWidgetAction>());
        rangeMeterWidgetActionMap.put(SemaphoreBehaviorWidgetAction.secondPosition, new ArrayList<SemaphoreBehaviorWidgetAction>());
        rangeMeterWidgetActionMap.put(SemaphoreBehaviorWidgetAction.thirdPosition, new ArrayList<SemaphoreBehaviorWidgetAction>());
        for (String[] graphicViewAction : getGraphicViewActions()) {
            String componentName = graphicViewAction[0];
            String permission = graphicViewAction[1];
            if (appIdentity.hasPermission(permission, VIEW_PERMISSION)) {
                //load component
                SemaphoreBehaviorWidgetAction component = (SemaphoreBehaviorWidgetAction) Component.getInstance(componentName);
                if (null != component) {

                    // put each action to a position according to its meter value
                    if (component.isInFirstInterval()) {
                        putToList(rangeMeterWidgetActionMap, SemaphoreBehaviorWidgetAction.firstPosition, component);
                    } else {
                        if (component.isInSecondInterval()) {
                            putToList(rangeMeterWidgetActionMap, SemaphoreBehaviorWidgetAction.secondPosition, component);
                        } else {
                            if (component.isInThirdInterval()) {
                                putToList(rangeMeterWidgetActionMap, SemaphoreBehaviorWidgetAction.thirdPosition, component);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<SemaphoreBehaviorWidgetAction> putToList(Map<Integer, List<SemaphoreBehaviorWidgetAction>> map, Integer key, SemaphoreBehaviorWidgetAction value) {
        if (!map.containsKey(key)) {
            List<SemaphoreBehaviorWidgetAction> list = new ArrayList<SemaphoreBehaviorWidgetAction>();
            list.add(value);
            return map.put(key, list);
        } else {
            map.get(key).add(value);
            return map.get(key);
        }
    }

    public Map<Integer, List<SemaphoreBehaviorWidgetAction>> getRangeMeterWidgetActionMap() {
        return rangeMeterWidgetActionMap;
    }

    protected String[][] getGraphicViewActions() {
        throw new UnsupportedOperationException("This method have to be implemented by child class...");
    }

    public int size(List list) {
        return list.size();
    }

    public double percent(int interval) {
        int sum = 0;
        for (List<SemaphoreBehaviorWidgetAction> meterWidgetViewActions : rangeMeterWidgetActionMap.values()) {
            sum += meterWidgetViewActions.size();
        }
        double intervalSize = rangeMeterWidgetActionMap.get(interval).size();
        Integer intResult = (new Double(intervalSize / sum * 10000)).intValue();
        return intResult.doubleValue() / 100;
    }

    public String color(int i) {
        if (i == SemaphoreBehaviorWidgetAction.firstPosition) {
            return "Common.color.green";
        } else if (i == SemaphoreBehaviorWidgetAction.secondPosition) {
            return "Common.color.yellow";
        } else if (i == SemaphoreBehaviorWidgetAction.thirdPosition) {
            return "Common.color.red";
        }
        return "";
    }
}
