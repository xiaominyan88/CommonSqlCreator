package sql.execute;

import javax.sql.DataSource;
import javax.sql.RowSet;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseExecute implements Execute {

    private DataSource dataSource;

    private RowSet rowSet;

    public BaseExecute(DataSource dataSource, RowSet rowSet){
        this.dataSource = dataSource;
        this.rowSet = rowSet;
    }

    @Override
    public <T> List<T> doQueryExecute(String executeSQL, String queryParams) throws Exception{
        return new ArrayList<T>();

    }
}
