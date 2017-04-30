package com.encens.khipus.action.treasury;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.FinancesDocumentType;
import com.encens.khipus.model.treasury.BankingMovementSync;
import com.encens.khipus.service.treasury.BankingMovementSyncService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.model.SelectItem;
import java.util.*;

/**
 * BankingMovementSyncAction
 *
 * @author
 * @version 2.9
 */
@Name("bankingMovementSyncAction")
@Scope(ScopeType.CONVERSATION)
public class BankingMovementSyncAction extends GenericAction<BankingMovementSync> {
    private String rowsSeparator = "\n";
    private File file = new File();
    private Map<Integer, FinancesDocumentType> documentTypeMapping = new HashMap<Integer, FinancesDocumentType>();
    private Map<Integer, Map<Integer, String>> mapDataContainer = new HashMap<Integer, Map<Integer, String>>();
    private Map<Integer, List<String>> mapContainerByRow = new HashMap<Integer, List<String>>();
    private List<SelectItem> columnPositionList = new ArrayList<SelectItem>();
    @In
    private BankingMovementSyncService bankingMovementSyncService;

    @Factory(value = "bankingMovementSync", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BANKINGMOVEMENTSYNC','VIEW')}")
    public BankingMovementSync initBankingMovementSync() {
        return getInstance();
    }

    public String getRowsSeparator() {
        return rowsSeparator;
    }

    public void setRowsSeparator(String rowsSeparator) {
        this.rowsSeparator = rowsSeparator;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Map<Integer, FinancesDocumentType> getDocumentTypeMapping() {
        return documentTypeMapping;
    }

    public void setDocumentTypeMapping(Map<Integer, FinancesDocumentType> documentTypeMapping) {
        this.documentTypeMapping = documentTypeMapping;
    }

    public Map<Integer, Map<Integer, String>> getMapDataContainer() {
        return mapDataContainer;
    }

    public void setMapDataContainer(Map<Integer, Map<Integer, String>> mapDataContainer) {
        this.mapDataContainer = mapDataContainer;
    }

    public Map<Integer, List<String>> getMapContainerByRow() {
        return mapContainerByRow;
    }

    public void setMapContainerByRow(Map<Integer, List<String>> mapContainerByRow) {
        this.mapContainerByRow = mapContainerByRow;
    }

    public List<SelectItem> getColumnPositionList() {
        return columnPositionList;
    }

    public void setColumnPositionList(List<SelectItem> columnPositionList) {
        this.columnPositionList = columnPositionList;
    }

    public String getDefaultColumnValue(Integer column) {
        if (!mapDataContainer.isEmpty() && !mapDataContainer.get(0).isEmpty()) {
            return mapDataContainer.get(0).get(column);
        }

        return null;
    }

    public List getDocumentTypeList() {
        return (getInstance().getDocumentTypePosition() != null) ?
                mapContainerByRow.get(getInstance().getDocumentTypePosition()) :
                new ArrayList();
    }

    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String nextInfo() {
        loadContentFile(getFile().getValue());
        if (mapDataContainer.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.requiredFile");
            return Outcome.REDISPLAY;
        }

        return Outcome.NEXT;
    }

    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String previousDefineColumns() {
        return Outcome.PREVIOUS;
    }

    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String nextDefineColumns() {
        if (!validateDefineColumns()) {
            return Outcome.REDISPLAY;
        }
        return Outcome.NEXT;
    }

    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String previousDocumentTypesRelationship() {
        return Outcome.PREVIOUS;
    }

    @End
    public String finalizeDocumentTypesRelationship() {
        if (!bankingMovementSyncService.registerBankingMovementSync(getInstance(), mapDataContainer, documentTypeMapping)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.registerProcess.fail");
            return Outcome.REDISPLAY;
        }
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "BankingMovementSync.registerProcess.success", mapDataContainer.size());
        return Outcome.SUCCESS;
    }

    public Boolean validateDefineColumns() {
        Boolean valid = true;
        for (String data : getMapContainerByRow().get(getInstance().getAccountNumberPosition())) {
            if (ValidatorUtil.isBlankOrNull(data)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.accountNumber");
                valid = false;
                break;
            }
        }
        for (String data : getMapContainerByRow().get(getInstance().getGlossPosition())) {
            if (ValidatorUtil.isBlankOrNull(data)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.gloss");
                valid = false;
                break;
            }
        }
        for (String data : getMapContainerByRow().get(getInstance().getDocumentTypePosition())) {
            if (ValidatorUtil.isBlankOrNull(data)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.documentType");
                valid = false;
                break;
            }
        }
        for (String data : getMapContainerByRow().get(getInstance().getDocumentNumberPosition())) {
            if (ValidatorUtil.isBlankOrNull(data)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.documentNumber");
                valid = false;
                break;
            }
        }
        for (String data : getMapContainerByRow().get(getInstance().getDatePosition())) {
            if (ValidatorUtil.isBlankOrNull(data) || !DateUtils.isValidPattern(data, getInstance().getDatePattern())) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.date", getInstance().getDatePattern());
                valid = false;
                break;
            }
        }
        for (String data : getMapContainerByRow().get(getInstance().getAmountPosition())) {
            if (!ValidatorUtil.isBigDecimal(data)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankingMovementSync.error.amount", getInstance().getDatePattern());
                valid = false;
                break;
            }
        }
        return valid;
    }

    private void loadContentFile(byte[] value) {
        if (value != null) {
            String stringValue = new String(value);
            StringTokenizer rowTokenizer = new StringTokenizer(stringValue, getRowsSeparator());
            mapDataContainer.clear();
            mapContainerByRow.clear();
            columnPositionList.clear();

            int row = 0;
            int columnsNumber = 0;
            while (rowTokenizer.hasMoreTokens()) {
                String rowTokenizerVar = rowTokenizer.nextToken();
                if (!ValidatorUtil.isBlankOrNull(rowTokenizerVar)) {
                    StringTokenizer columnTokenizer = new StringTokenizer(rowTokenizerVar, getInstance().getColumnSeparator());
                    Map<Integer, String> rowContainer = new HashMap<Integer, String>();
                    int column = 0;
                    while (columnTokenizer.hasMoreTokens()) {
                        String columnValue = columnTokenizer.nextToken().trim();
                        rowContainer.put(column, columnValue);
                        addValue(mapContainerByRow, column, columnValue);
                        column++;
                    }
                    if (!rowContainer.isEmpty()) {
                        if (columnsNumber == 0) {
                            columnsNumber = column;
                        }
                        mapDataContainer.put(row, rowContainer);
                        row++;
                    }
                }
            }

            for (int x = 0; x < columnsNumber; x++) {
                columnPositionList.add(new SelectItem(x, MessageUtils.getMessage("BankingMovementSync.column", (x + 1))));
            }
        }
    }

    private void addValue(Map<Integer, List<String>> map, Integer key, String value) {
        List list = map.get(key);
        if (list == null) {
            list = new ArrayList<String>();
        }
        if (!list.contains(value)) {
            list.add(value);
        }
        map.put(key, list);
    }
}
