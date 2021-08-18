package sql.creator;


public interface Creator {

    public String querySQL(String tableName,String... args);

    public String insertSQL(String tableName,String... args);

    public String updateSQL(String tableName,String setCols,String conCols);

    public String replaceSQL(String tableName,String... args);

}
