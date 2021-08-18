package sql.execute;

import javax.sql.DataSource;
import javax.sql.RowSet;
import java.util.List;

@SuppressWarnings({"unchecked","deprecate"})
public interface Execute {

    public <T> List<T> doQueryExecute(String executeSQL,String queryParams) throws Exception;

    public <T> int doModifyExecute(DataSource dataSource, List<T> queryResult,String executeSQL,String replaceParams) throws Exception;

}
