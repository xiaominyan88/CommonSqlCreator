package process;


import annotation.BeanField;
import annotation.BeanGetMethod;
import annotation.BeanSetMethod;
import org.apache.commons.lang3.ArrayUtils;
import predictions.Predictions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

/**
 *Includes some methods to create kinds of SQLs for making query, updating tables and insert result into the table you defined
 * and the methods to execute the SQLs which return the result with a list of UDF Object
 * 本类主要用于实现对数据库的访问方法
 * @author xiaominyan 18600885092
 *
 */

public class WrapperProcess {

    /**
     * private fields of WrapperProccess Class
     */
    private boolean debug = false;

    /**
     * Constructors of WrapperProcess with arguments
     * @param debug arguments to change the private fields debug mode
     */
    public WrapperProcess(boolean debug){
        this.debug = debug;
    }

    private void dPrint(String msg) {
        if (debug) {
            System.err.println("Message Info : " + msg);
        }
    }

    /**
     * return the result {@code String} in specific format,for example,
     * when "1" equals flag
     * the result format is
     * aaa,bbb,ccc
     * when "2" equals flag
     * the result format is
     * 'aaa','bbb','ccc'
     * @param list the {@code List<String>} you want to make a change
     * @param flag the format factor
     * @return character strings with specific format
     */
    private String listToString(List<String> list,String flag){
        StringBuilder sb = new StringBuilder();
        switch(flag){
            case "1":
                list.forEach(x->sb.append(x + ","));
                return sb.substring(0,sb.length()-1);
            case "2":
                list.forEach(x->sb.append("'" + x + "',"));
                return sb.substring(0,sb.length()-1);
            default:
                return "lack of parameter";
        }
    }



    /**
     * return {@code List<T>} the elements of which is ordered by the instance of the second arguements
     * that associated with UDF annotation{@code BeanField, @code BeanSetMethod, @code BeanGetMethod}
     * @param objects generic array with input data
     * @param clazz depends on that UDF annotation to sort and filter the array
     * @param <T> generic variables
     * @return Collection of sorted and filted elements of array
     * @throws Exception
     */
    private <T> List<T> getOrderCollection(T[] objects, Class<?> clazz) throws Exception{
        List<T> list = new ArrayList<>();

        Predictions.checkArgument(!(clazz.isAssignableFrom(BeanField.class)
                || clazz.isAssignableFrom(BeanSetMethod.class)
                || clazz.isAssignableFrom(BeanGetMethod.class)),
                "the second arguments must be assignable " +
                        "among BeanFiled,BeanSetMethod and BeanGetMethod");

        if(clazz.isAssignableFrom(BeanField.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Field> fieldClass = m.getClass().asSubclass(Field.class);
                if(fieldClass.getAnnotation(BeanField.class) != null){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Field.class).getAnnotation(BeanField.class).order()
            ));
        }else if(clazz.isAssignableFrom(BeanSetMethod.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Method> methodClass = m.getClass().asSubclass(Method.class);
                if(methodClass.getAnnotation(BeanSetMethod.class) != null){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Method.class).getAnnotation(BeanSetMethod.class).order()
            ));
        }else if(clazz.isAssignableFrom(BeanGetMethod.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Method> methodClass = m.getClass().asSubclass(Method.class);
                if(methodClass.getAnnotation(BeanGetMethod.class) != null){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Method.class).getAnnotation(BeanGetMethod.class).order()
            ));
        }
        return list;
    }


    /**
     * return {@code List<T>} the elements of which is ordered by the instance of the second arguements
     * that associated with UDF annotation{@code BeanField, @code BeanSetMethod, @code BeanGetMethod}
     * and filtered by the last argument stands for the fields of the selected table
     * @param objects generic array with input data
     * @param clazz depends on that UDF annotation to sort and filter the array
     * @param <T> generic variables
     * @param args fields of selected table
     * @return Collection of sorted and filted elements of array
     * @throws Exception
     */
    private <T> List<T> getOrderFilterCollection(T[] objects, Class<?> clazz, String[] args) throws Exception{
        List<T> list = new ArrayList<>();
        if(clazz.isAssignableFrom(BeanField.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Field> fieldClass = m.getClass().asSubclass(Field.class);
                if(fieldClass.getAnnotation(BeanField.class) != null
                        && Arrays.asList(args).contains(fieldClass.getName())){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Field.class).getAnnotation(BeanField.class).order()
            ));
        }else if(clazz.isAssignableFrom(BeanSetMethod.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Method> methodClass = m.getClass().asSubclass(Method.class);
                if(methodClass.getAnnotation(BeanSetMethod.class) != null
                        && Arrays.asList(args).contains(methodClass.getName().substring(3).toLowerCase())){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Method.class).getAnnotation(BeanSetMethod.class).order()
            ));
        }else if(clazz.isAssignableFrom(BeanGetMethod.class)){
            Arrays.asList(objects).forEach(m -> {
                Class<? extends Method> methodClass = m.getClass().asSubclass(Method.class);
                if(methodClass.getAnnotation(BeanGetMethod.class) != null
                        && Arrays.asList(args).contains(methodClass.getName().substring(3).toLowerCase())){
                    list.add(m);
                }
            });
            list.sort(Comparator.comparingInt(
                    m -> m.getClass().asSubclass(Method.class).getAnnotation(BeanGetMethod.class).order()
            ));
        }else{
            dPrint("the second arguments must be assignable among BeanFiled,BeanSetMethod and BeanGetMethod");
            throw new Exception("The second argument is not correct");
        }
        return list;
    }




    /**
     * this mehtod provides a common sql execute method and it will return result as Class POJO,which is defined by the parameter path, result type is List<Object>
     *
     *
     * @param conn database connection
     * @param prst PreparedStatement
     * @param rs result set for sql query
     * @param sql the query sql
     * @param path the path for user-defined Bean such as "com.XXX.XXX.bean.StationInfoBean"
     * @param parameterList the field List that you want to make a query
     * @return a list of UDF Object that is evaluated by the query
     */
    public List<Object> selectSQLExecuteQuery(Connection conn,PreparedStatement prst,ResultSet rs,String sql, String path,List<String> parameterList){
        List<Object> list1 = new ArrayList<Object>();
        try{
            System.out.println(sql);
            prst = conn.prepareStatement(sql);
            rs = prst.executeQuery();
            while(rs.next()){
                Class claz = Class.forName(path);
                Object si = claz.newInstance();
                Field[] fields = claz.getDeclaredFields();
                Method[] methods = claz.getDeclaredMethods();
                List<Field> fieldList = getOrderCollection(fields,BeanField.class);
                List<Method> setMethodList = getOrderCollection(methods,BeanSetMethod.class);
                for(int i = 0; i < fieldList.size(); i++){
                    if(parameterList.contains(fieldList.get(i).getName())){
                        if((int.class).equals(fieldList.get(i).getType())){
                            setMethodList.get(i).invoke(si,rs.getInt(fieldList.get(i).getName()));
                        }else if((String.class).equals(fieldList.get(i).getType())){
                            setMethodList.get(i).invoke(si,rs.getString(fieldList.get(i).getName()));
                        }else if((double.class).equals(fieldList.get(i).getType())){
                            setMethodList.get(i).invoke(si,rs.getDouble(fieldList.get(i).getName()));
                        }else if((Timestamp.class).equals(fieldList.get(i).getType())){
                            setMethodList.get(i).invoke(si,rs.getTimestamp(fieldList.get(i).getName()));
                        }
                    }
                }
                list1.add(si);
            }

            rs.close();
            prst.close();


        }  catch (Exception e) {
            e.printStackTrace();
        }
        return list1;

    }

    /**
     * this method is to replace data from @List<? extends Object> into the table
     * @param conn database connection
     * @param prst preparedstatement
     * @param sql the sql for replace the table field's data
     * @param list the set of any class extending Object class
     * @param batchNum the batch times you make a replace action
     * @param args the table field
     */
    public void replaceSQLExecuteBatch(Connection conn,PreparedStatement prst,String sql,List<? extends Object> list,int batchNum,String[] args){
        try{
            int j = 0;
            System.out.println(sql);
            prst = conn.prepareStatement(sql);
            for(int m = 0;m < list.size(); m++){
                List<Field> fieldList1 = new ArrayList<Field>();
                List<Method> getMethodList1 = new ArrayList<Method>();
                Method [] methods = list.get(m).getClass().getDeclaredMethods();
                Field[] fields = list.get(m).getClass().getDeclaredFields();
                List<Field> fieldList = getOrderFilterCollection(fields,BeanField.class,args);
                List<Method> getMethodList = getOrderFilterCollection(methods,BeanGetMethod.class,args);
                for(int i = 0; i<fieldList1.size();i++){
                    if((String.class).equals(fieldList1.get(i).getType())){
                        prst.setString(i+1,(String)getMethodList1.get(i).invoke(list.get(m)));
                    }else if((double.class).equals(fieldList1.get(i).getType())){
                        prst.setDouble(i+1,(double)getMethodList1.get(i).invoke(list.get(m)));
                    }else if((int.class).equals(fieldList1.get(i).getType())){
                        prst.setInt(i+1,(int)getMethodList1.get(i).invoke(list.get(m)));
                    }else if((Timestamp.class).equals(fieldList1.get(i).getType())){
                        prst.setTimestamp(i+1,(Timestamp)getMethodList1.get(i).invoke(list.get(m)));
                    }
                }
                prst.execute();

                j++;
                if(j % batchNum == 0){

                    conn.commit();

                }
            }
            conn.commit();

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    /**
     * this method is to execute the update of the table's field with batch
     * @param conn database connection
     * @param prst preparedstatement
     * @param sql the sql for update table's fields
     * @param list parameter {@code List<? extends Object>} is list of UDF Object
     * @param batchNum the number of each batch
     * @param setField arrays of table set you want to update
     * @param conField arrays of table set which is limited condition
     */
    public void updateSQLExecuteBatch(Connection conn, PreparedStatement prst, String sql, List<? extends Object> list,int batchNum, String[] setField, String[] conField){
        String[] sumField = ArrayUtils.addAll(setField,conField);
        replaceSQLExecuteBatch(conn,prst,sql,list,batchNum,sumField);
    }


    /**
     * this method is to create a sql for query the whole table whithout any limited condition
     * @param tableName the table name
     * @param args the fields you want to make query
     * @return
     */
    public String selectSQLWhithoutCondition(String tableName,String[] args){
        StringBuilder sb = new StringBuilder();
        Arrays.asList(args).forEach(x->sb.append(x + ","));
        return ("select " + sb.substring(0,sb.length()-1) + " from " + tableName);
    }

    /**
     * this method is to create a replace sql whith place holder,for example "replace into xxx (XXX,XXX,XXX) values ( ?,?,?)"
     * @param map the field switcher,which allowed you to change the field name,this parameter can be empty,for example ,
     *            if you want to turn a field's name YYY into XXX, your should initialize a Map<String,String> map and put("YYY","XXX"),
     *            and put the map into this method
     * @param tableName the table name
     * @param args the fields you want to insert into the table
     * @return
     */
    public String insertSQLWithPlaceHolder(Map<String,String> map,String tableName,String[] args){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        if(map.isEmpty() || map.size() == 0){
            Arrays.asList(args).forEach(x->{
                sb1.append(x + ",");
                sb2.append("?,");
            });
        }else{
            Arrays.asList(args).forEach(
                    x->{
                        if (map.get(x) != null) {
                            x = map.get(x);
                        }
                        sb1.append(x + ",");
                        sb2.append("?,");
                    }
            );
        }
        return ("replace into " + tableName + " (" + sb1.substring(0,sb1.length()-1) + ") values (" + sb2.substring(0,sb2.length()-1) + ")");

    }

    /**
     * this method is to create a sql for updating the infomation with certain limited condition in the table
     *
     * @param tableName the table name
     * @param setField arrays of the set field you want to make update
     * @param conField arrays of the set field which is considered as limited condition
     * @return the sql that you want to make update with limited condition
     */
    public String updateSQLWithPlaceHolder(String tableName,String[] setField, String[] conField){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for(int i = 0; i < setField.length; i++){
            sb1.append(setField[i] + "=?,");
        }

        for(int i = 0; i < conField.length; i++){
            sb2.append(conField[i] + "=? and ");
        }
        return ("update " + tableName + " SET " + sb1.substring(0,sb1.length()-1) + " WHERE " + sb2.substring(0,sb2.length()-5));

    }


    /**
     * this method is to create a sql for query with specific limited condition
     * @param conditionMap the map that links the limited condition
     * @param tableName the table name
     * @param symbol the filter symbol, for example, =,!=
     * @param args the field in the table you want to query
     * @return the sql with specific limited condition you want to make query
     */
    public String selectSQLWithFilter(Map<String,String> conditionMap,String tableName,String symbol,String... args){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        Arrays.asList(args).forEach(x-> sb1.append(x).append(","));
        for(Map.Entry<String,String> entry : conditionMap.entrySet()){
            sb2.append(entry.getKey()).append(symbol).append(entry.getValue()).append(" and ");
        }
        return ("select " + sb1.substring(0,sb1.length()-1) + " from " + tableName + " where " + sb2.substring(0,sb2.length()-5));
    }


    /**
     * this method is to create lots of sql with union all condition for query
     * @param tableMap the mapping of tablename and query field in each sql
     * @param list the empty list for adding all the unique fields in sql
     * @return the sql with the union of different tables
     */
    public String selectSQLWithUnionAll(Map<String,String[]> tableMap,List<String> list){
        StringBuilder sb = new StringBuilder();
        for (String[] value : tableMap.values()) {
            list.addAll(Arrays.asList(value));
        }
        HashSet set = new HashSet(list);
        list.clear();
        list.addAll(set);
        for (Map.Entry<String, String[]> entry : tableMap.entrySet()) {
            StringBuffer sb1 = new StringBuffer();
            for(String element : list){
                if(Arrays.asList(entry.getValue()).contains(element)){
                    sb1.append(element).append(",");
                }else{
                    sb1.append("'' as ").append(element).append(",");
                }
            }
            sb.append("select ").append(sb1.substring(0,sb1.length()-1)).append(" from ").append(entry.getKey()).append(" union all ");
        }
        return sb.substring(0,sb.length()-11);
    }


    /**
     * this method is to create a sql with different subquery for query
     *
     * @param tableMap the mapping of table name and the array of fields you want to query in the table
     * @param conMap the mapping of each table's field
     * @param resultMap the mapping of the name and each table's field
     * @param list the empty {@code List<String}
     * @return
     */
    public String selectSQLFromMultiTableWithCondition(Map<String,String[]> tableMap,Map<String,String> conMap,Map<String,String> resultMap,List<String> list){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = new StringBuilder();
        for(Map.Entry<String,String[]> entry : tableMap.entrySet()){
            sb1.append("(select ").append(listToString(Arrays.asList(entry.getValue()),"1")).append(" from ").append(entry.getKey()).append(") ").append(entry.getKey()).append(",");
        }
        for(Map.Entry<String,String> entry : conMap.entrySet()){
            sb2.append(entry.getKey()).append(" = ").append(entry.getValue()).append(" and ");
        }
        for(Map.Entry<String,String> entry : resultMap.entrySet()){
            sb3.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
            list.add(entry.getValue());
        }
        return ("select " + sb3.substring(0,sb3.length()-1) + " from " + sb1.substring(0,sb1.length()-1) + " where " + sb2.substring(0,sb2.length()-5));

    }

    /**
     * this method is combined with a sql creatation method and a sql exectutation method
     * @param conn database connection
     * @param prst
     * @param rs
     * @param tableName
     * @param path
     * @param arg
     * @return
     */
    public List<Object> selectInfoWithoutConditionMethod(Connection conn, PreparedStatement prst, ResultSet rs, String tableName,String path,String... arg){
        String sql = selectSQLWhithoutCondition(tableName,arg);
        return selectSQLExecuteQuery(conn,prst,rs,sql,path,Arrays.asList(arg));
    }


    public void insertInfoMethod(Connection conn, PreparedStatement prst, List<? extends Object> list,Map<String,String> map,String tableName,String... args){
        String sql = insertSQLWithPlaceHolder(map,tableName,args);
        replaceSQLExecuteBatch(conn,prst,sql,list,1000,args);
    }


    public void updateTableInfoWithCondition(Connection conn,PreparedStatement prst,String tableName, List<? extends Object> list, String[] setField, String[] conField){
        String sql = updateSQLWithPlaceHolder(tableName,setField,conField);
        updateSQLExecuteBatch(conn,prst,sql,list,1000,setField,conField);
    }


    public List<Object> selectInfoWithAndCondition(Connection conn,PreparedStatement prst,ResultSet rs,String tableName,String path,String symbol,Map<String,String> conditionMap,String... args){
        String sql = selectSQLWithFilter(conditionMap,tableName,symbol,args);
        return selectSQLExecuteQuery(conn,prst,rs,sql,path,Arrays.asList(args));
    }


    public List<Object> selectInfoUnionAllTables(Connection conn,PreparedStatement prst,ResultSet rs,String path,Map<String,String[]> tableMap){
        List<String> list = new ArrayList<String>();
        String sql = selectSQLWithUnionAll(tableMap,list);
        return selectSQLExecuteQuery(conn,prst,rs,sql,path,list);
    }


    public List<Object> selectInfoMultiTableWithCondition(Connection conn,PreparedStatement prst,ResultSet rs,String path,Map<String,String[]> tableMap, Map<String,String> conMap,Map<String,String> resultMap){
        List<String> list = new ArrayList<>();
        String sql = selectSQLFromMultiTableWithCondition(tableMap,conMap,resultMap,list);
        return selectSQLExecuteQuery(conn,prst,rs,sql,path,list);
    }




}
