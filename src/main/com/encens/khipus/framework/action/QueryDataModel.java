package com.encens.khipus.framework.action;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.ListEntityManagerName;
import com.encens.khipus.util.query.QueryUtils;
import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.jboss.seam.Component;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base DataModel to be used in View DataTables.
 * It allows to define a sorting property  and its order type.
 * It allows to bind the page for the datascroller, if there's one bind to it
 * It browse the data in a paged fashion.
 * It's recommended to use this databmodel binded to a component with page scope.
 *
 * @author
 * @version $Id: BaseDataModel.java 2008-8-20 16:26:26 $
 */
public abstract class QueryDataModel<ID, T extends BaseModel> extends SerializableDataModel {

    /**
     * To configure the entity manager with which execute the Entity query.
     * <p/>
     * The default value its 'listEntityManager'
     */
    private String entityManagerName = ListEntityManagerName.DEFAULT_LIST.getName();


    private ID currentId;
    private boolean detached = false;
    private List<ID> wrappedKeys = null;
    private final Map<ID, T> wrappedData = new HashMap<ID, T>();
    private Integer rowCount;
    private int numberOfRows;
    private int numberOfRowsDisplayed;
    private int rowIndex;
    protected String sortProperty;
    private String currentSortProperties = null;
    protected boolean sortAsc = true;
    private int page = 1;

    private Class<T> entityClass;
    private Class<ID> idClass;
    //TODO: improve this, entityQuery does not require to be serialized , it must be transient, but making it transient gives unexpected results when searching
    private EntityQuery<T> entityQuery;
    private String ejbql = null;
    private List<String> restrictions = new ArrayList<String>(0);
    private T criteria;
    private Map<Integer, Boolean> selectedValue = new HashMap<Integer, Boolean>();
    private Map<Integer, Map<ID, Boolean>> selectedList = new HashMap<Integer, Map<ID, Boolean>>();

    public Long getCount() {
        initEntityQuery();
        postInitEntityQuery(entityQuery);
        entityQuery.setOrder(getOrder());
        return entityQuery.getResultCount();
    }


    public List<T> getList(Integer firstRow, Integer maxResults) {
        initEntityQuery();
        postInitEntityQuery(entityQuery);
        setNumberOfRows(maxResults);
        entityQuery.setOrder(getOrder());
        entityQuery.setFirstResult(firstRow);
        entityQuery.setMaxResults(maxResults);
        return entityQuery.getResultList();
    }

    private void initEntityQuery() {
        if (entityQuery == null) {
            entityQuery = new EntityQuery<T>();
            entityQuery.setEjbql(getEjbql());
            entityQuery.setRestrictionExpressionStrings(getRestrictions());
            criteria = createInstance();
        }
        entityQuery.setEntityManager(getEntityManager());
    }

    protected void postInitEntityQuery(EntityQuery entityQuery) {

    }

    public List<T> getResultList() {
        initEntityQuery();
        postInitEntityQuery(entityQuery);
        entityQuery.setOrder(getOrder());
        return entityQuery.getResultList();
    }

    private String getOrder() {
        if (getSortProperty() != null) {
            String currentOrder = isSortAsc() ? " ASC" : " DESC";
            currentSortProperties = getSortProperty().replaceAll(" ", "").replaceAll(",", currentOrder + ",") + currentOrder;
        }
        return currentSortProperties;
    }

    public String getEjbql() {
        return ejbql;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

    public T getCriteria() {
        return criteria;
    }

    public void setCriteria(T criteria) {
        this.criteria = criteria;
    }

    public void search() {
        if (entityQuery != null) {
            entityQuery.refresh();
        }
        setPage(1);
        clearAllSelection();
    }

    public void clear() {
        setCriteria(createInstance());
        clearAllSelection();
        updateAndSearch();
    }

    public void updateAndSearch() {
        update();
        search();
    }

    public void reset() {
        updateAndSearch();
    }

    public T createInstance() {
        if (getEntityClass() != null) {
            try {
                criteria = getEntityClass().newInstance();
                return criteria;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {

            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public Class<T> getEntityClass() {
        if (entityClass == null) {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                entityClass = (Class<T>) paramType.getActualTypeArguments()[1];
            } else {
                throw new IllegalArgumentException("Could not guess entity class by reflection");
            }
        }
        return entityClass;
    }

    @SuppressWarnings({"unchecked"})
    public Class<ID> getIdClass() {
        if (idClass == null) {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                idClass = (Class<ID>) paramType.getActualTypeArguments()[0];
            } else {
                throw new IllegalArgumentException("Could not guess id class by reflection");
            }
        }
        return idClass;
    }

    public void walk(FacesContext facesContext, DataVisitor dataVisitor, Range range, Object argument) throws IOException {
        if (detached) {
            for (ID key : wrappedKeys) {
                setRowKey(key);
                dataVisitor.process(facesContext, key, argument);
            }
        } else {
            int firstRow = ((SequenceRange) range).getFirstRow();
            int numberOfRows = ((SequenceRange) range).getRows();
            wrappedKeys = new ArrayList<ID>();

            for (T instance : getList(firstRow, numberOfRows)) {
                wrappedKeys.add(getId(instance));
                wrappedData.put(getId(instance), instance);
                dataVisitor.process(facesContext, getId(instance), argument);
            }
        }
        setNumberOfRowsDisplayed(wrappedKeys.size());
    }

    @Override
    public SerializableDataModel getSerializableModel(Range range) {
        if (wrappedKeys != null) {
            detached = true;
            return this;
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public ID getId(T row) {
        return (ID) row.getId();
    }


    public void update() {
        rowCount = null;
        detached = false;
    }

    public boolean isRowAvailable() {
        if (currentId == null) {
            return false;
        } else if (wrappedKeys.contains(currentId)) {
            return true;
        } else if (wrappedData.keySet().contains(currentId)) {
            return true;
        }
        return false;
    }

    public Object getRowData() {
        if (currentId == null) {
            return null;
        } else {
            return wrappedData.get(currentId);
        }
    }

    public int getRowCount() {
        if (rowCount == null) {
            rowCount = getCount().intValue();
        }
        return rowCount;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNumberOfRowsDisplayed() {
        return numberOfRowsDisplayed;
    }

    public void setNumberOfRowsDisplayed(int numberOfRowsDisplayed) {
        this.numberOfRowsDisplayed = numberOfRowsDisplayed;
    }

    public Integer getCurrentRows() {
        if (getRowCount() > getNumberOfRows() && (getPage() * getNumberOfRows()) <= getRowCount() && getPage() * getNumberOfRows() > 0) {
            return getPage() * getNumberOfRows();
        }
        return getRowCount();
    }

    public String getRangeRowsString() {
        Integer currentRows = getCurrentRows();
        if (currentRows != null && currentRows > 0) {
            return FormatUtils.removePoint(((getNumberOfRowsDisplayed() > 0 ? currentRows - getNumberOfRowsDisplayed() : 0) + 1) + " - " + currentRows);
        }
        return "0";
    }

    public String getRowCountString() {
        return FormatUtils.removePoint("" + getRowCount());
    }

    @SuppressWarnings({"unchecked"})
    public void setRowKey(Object key) {
        this.currentId = (ID) key;
    }

    public Object getRowKey() {
        return currentId;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }

    public void setWrappedData(Object o) {
        throw new UnsupportedOperationException();
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        if (this.sortProperty != null) {
            setSortAsc(!this.sortProperty.equals(sortProperty) || !isSortAsc());
        }
        this.sortProperty = sortProperty;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    protected EntityManager getEntityManager() {
        return (EntityManager) Component.getInstance(entityManagerName);
    }

    public Map<Integer, Boolean> getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(Map<Integer, Boolean> selectedValue) {
        this.selectedValue = selectedValue;
    }

    public Map<Integer, Map<ID, Boolean>> getSelectedList() {
        if (selectedList.get(getPage()) == null) {
            selectedList.put(getPage(), new HashMap<ID, Boolean>());
        }
        return selectedList;
    }

    public void setSelectedList(Map<Integer, Map<ID, Boolean>> selectedList) {
        this.selectedList = selectedList;
    }

    @SuppressWarnings({"unchecked"})
    public List<T> getSelectedObjectList() {
        List<T> currentSelectionList = new ArrayList<T>();

        if (!getIdClass().isLocalClass()) {
            List<ID> currentIdSelectionList = getSelectedIdList();
            if (!currentIdSelectionList.isEmpty()) {
                currentSelectionList = QueryUtils.selectAllIn(getEntityManager(), getEntityClass(), currentIdSelectionList).getResultList();
            }
        } else {
            for (Map.Entry<Integer, Map<ID, Boolean>> mainEntrySelection : getSelectedList().entrySet()) {
                for (Map.Entry<ID, Boolean> selectEntry : mainEntrySelection.getValue().entrySet()) {
                    if (selectEntry.getValue()) {
                        currentSelectionList.add(getEntityManager().find(getEntityClass(), selectEntry.getKey()));
                    }
                }
            }
        }
        return currentSelectionList;
    }

    public List<ID> getSelectedIdList() {
        List<ID> currentSelectionList = new ArrayList<ID>();
        for (Map.Entry<Integer, Map<ID, Boolean>> mainEntrySelection : getSelectedList().entrySet()) {
            for (Map.Entry<ID, Boolean> selectEntry : mainEntrySelection.getValue().entrySet()) {
                if (selectEntry.getValue()) {
                    currentSelectionList.add(selectEntry.getKey());
                }
            }
        }
        return currentSelectionList;
    }

    public void selectAll() {
        for (Map.Entry<ID, Boolean> selectEntry : getSelectedList().get(getPage()).entrySet()) {
            selectEntry.setValue(getSelectedValue().get(getPage()));
        }
    }

    public void clearAllSelection() {
        getSelectedValue().clear();
        getSelectedList().clear();
    }

    public String getEntityManagerName() {
        return entityManagerName;
    }

    public void setEntityManagerName(String entityManagerName) {
        this.entityManagerName = entityManagerName;
    }
}