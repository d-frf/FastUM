package dss.uminho.FastDL;

import java.sql.*;
import java.util.*;

public abstract class AbstractDAO<V> implements Map<String,V> {

    protected Connection connection;
    private final String tableName;
    private final String keyName;

    public AbstractDAO(String tableName, String keyName) {
        this.tableName = tableName;
        this.keyName = keyName;
        try{
            this.connection = DriverManager.getConnection(
                    DAOconfig.URL,
                    DAOconfig.USERNAME,
                    DAOconfig.PASSWORD
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear(){
        try(Statement st = this.connection.createStatement()){
            st.executeUpdate("DELETE FROM " + this.tableName + " WHERE TRUE");
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public boolean containsKey(Object key){
        boolean res = false;

        if ( key instanceof String && key != null ){
            try (PreparedStatement st = this.connection.prepareStatement(
                    "SELECT " + this.keyName + " FROM " + this.tableName + " WHERE " +
                            this.keyName + "=?")){
                st.setString(1, (String) key);

                try (ResultSet rs = st.executeQuery()) {
                    res = rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    public abstract boolean containsValue(Object value);

    public Set<Entry<String,V>> entrySet(){

        Set<Entry<String,V>> entries = new HashSet<>();

        try (Statement st = this.connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM " + this.tableName)){

            while (rs.next()){
                entries.add(Map.entry(rs.getString(1),this.decodeTuple(rs)));
            }

        } catch (SQLException e){
            throw new RuntimeException(e);
        }

        return entries;

    }


    public V get(Object key){

        V res = null;

        if ( key instanceof String && key != null ) {

            try (PreparedStatement st = this.connection.prepareStatement(
                    "SELECT * FROM " + this.tableName + " WHERE " + this.keyName + " = ?"
            )){

                st.setString(1,(String) key);

                try ( ResultSet rs = st.executeQuery() ) {
                    if (rs.next()){
                        res = this.decodeTuple(rs);
                    }
                }

            } catch (SQLException e){
                throw new RuntimeException(e);
            }

        }

        return res;
    }

    public boolean isEmpty(){
        return this.size() == 0;
    }

    public Set<String> keySet(){

        HashSet<String> ret = new HashSet<>();

        try ( Statement st = this.connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT " + this.keyName + " FROM " + this.tableName)) {

            while ( rs.next() ){

                ret.add(rs.getString(1));

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    public abstract V put(String key,V value);

    /**
     * @return Número de elementos guardados em base de dados
     */
    @Override
    public int size() {
        try( PreparedStatement st = this.connection.prepareStatement(
                "SELECT COUNT(*) FROM " + this.tableName
        );
        ResultSet rs = st.executeQuery()){

            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param o 
     * @return
     */
    @Override
    public V remove(Object key) {

        boolean turnAutoCommitBackOn = false;

        try {
            turnAutoCommitBackOn = this.connection.getAutoCommit();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        try {

            this.connection.setAutoCommit(false);

            V ret = this.get(key);

            try (PreparedStatement st = this.connection.prepareStatement(
                    "DELETE FROM " + this.tableName + " WHERE " + this.keyName + "=?"
            )) {
                st.setString(1,(String) key);
                st.executeUpdate();
            }

            this.connection.commit();
            return ret;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if ( turnAutoCommitBackOn )
                    this.connection.setAutoCommit(true);
            } catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param map 
     */
    @Override
    public void putAll(Map<? extends String, ? extends V> map) {

        boolean turnAutoCommitBackOn = true;

        try {
            turnAutoCommitBackOn = this.connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try{
            this.connection.setAutoCommit(false);

            for(Map.Entry<? extends String,? extends V> entry : map.entrySet())
                this.put(entry.getKey(),entry.getValue());

            this.connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try{
                if (turnAutoCommitBackOn)
                    this.connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @return 
     */
    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();

        try ( Statement st = this.connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM " + this.tableName)){

            while( rs.next() )
                values.add(this.decodeTuple(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return values;
    }

    protected abstract V decodeTuple(ResultSet tuple) throws SQLException;
}
