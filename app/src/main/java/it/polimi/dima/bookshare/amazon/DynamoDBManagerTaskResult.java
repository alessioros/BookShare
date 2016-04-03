package it.polimi.dima.bookshare.amazon;

/**
 * Created by matteo on 26/03/16.
 */
public class DynamoDBManagerTaskResult {
    private DynamoDBManagerType taskType;
    private String tableStatus;

    public DynamoDBManagerType getTaskType() {
        return taskType;
    }

    public void setTaskType(DynamoDBManagerType taskType) {
        this.taskType = taskType;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }
}
