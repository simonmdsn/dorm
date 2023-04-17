import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dorm<T> implements Queries<T, Integer> {

    private final Connection connection;
    private final Class<T> clazz;
    private String idFieldName;
    private Field idField;
    private final List<String> nonIdFieldNames = new ArrayList<>();
    private final List<String> allFieldNames = new ArrayList<>();
    private final List<String> nonIdFieldTypes = new ArrayList<>();
    private String tableName;


    public Dorm(Connection connection, Class<T> clazz) {
        this.connection = connection;
        this.clazz = clazz;
        parseClass();
    }

    private void parseClass() {
        if (clazz.isAnnotationPresent(Table.class)) {
            this.tableName = clazz.getAnnotation(Table.class).name();
            Field[] fields = clazz.getDeclaredFields();
            Type idType = null;
            this.idFieldName = null;
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                    idType = field.getGenericType();
                    idFieldName = field.getName();
                } else {
                    nonIdFieldNames.add(field.getName());
                    nonIdFieldTypes.add(field.getGenericType().getTypeName());
                }
                allFieldNames.add(field.getName());
            }
            if (idType == null) {
                throw new RuntimeException("No id field found for class " + clazz.getName());
            }
            System.out.println("Creating table if not exists...");
            try {
                List<String> nonIdFields = new ArrayList<>();
                for (int i = 0; i < nonIdFieldNames.size(); i++) {
                    System.out.println(nonIdFieldNames.get(i) + " " + TypeMapper.map(nonIdFieldTypes.get(i)));
                    nonIdFields.add(nonIdFieldNames.get(i) + " " + TypeMapper.map(nonIdFieldTypes.get(i)));
                }
                PreparedStatement preparedStatement =
                        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + idFieldName + " serial primary " +
                                                            "key," + String.join(",", nonIdFields) + ")");
                System.out.println("Executing: " + preparedStatement.toString());
                boolean success = preparedStatement.execute();
                if (success) {
                    System.out.println("Table created successfully");
                } else {
                    System.out.println("Table already exists");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Class " + clazz.getName() + " is not annotated with @Table");
        }
    }

    @Override
    public boolean insert(T t) {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "INSERT INTO " + tableName + " (" + nonIdFieldNames.stream().map(String::toLowerCase).collect(Collectors.joining(",")) + ")" +
                                    " VALUES " +
                                    "(" + nonIdFieldNames.stream().map(e -> "?").collect(Collectors.joining(",")) + ")"
                    );
            for (int i = 0; i < nonIdFieldNames.size(); i++) {
                Field field = clazz.getDeclaredField(nonIdFieldNames.get(i));
                field.setAccessible(true);
                preparedStatement.setObject(i + 1, field.get(t));
            }
            System.out.println("Executing: " + preparedStatement.toString());
            System.out.println("Inserting " + clazz.getName() + " into " + tableName + "...");
            return preparedStatement.execute();
        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public T selectById(Integer integer) {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + idFieldName + " = " + integer);
            System.out.println("Executing: " + preparedStatement.toString());
            System.out.println("Selecting " + clazz.getName() + " with id " + integer + " from " + tableName + "...");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                T object = clazz.getConstructor().newInstance();
                for (int i = 0; i < allFieldNames.size(); i++) {
                    Field field = clazz.getDeclaredField(allFieldNames.get(i));
                    field.setAccessible(true);
                    field.set(object, resultSet.getObject(i + 1));
                }
                return object;
            }
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find" + clazz.getName() + " with id " + integer);
        return null;
    }

    @Override
    public List<T> selectAll() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tableName);
            System.out.println("Executing: " + preparedStatement.toString());
            System.out.println("Selecting all " + clazz.getName() + " from " + tableName + "...");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                T object = clazz.getConstructor().newInstance();
                for (int i = 0; i < allFieldNames.size(); i++) {
                    Field field = clazz.getDeclaredField(allFieldNames.get(i));
                    field.setAccessible(true);
                    field.set(object, resultSet.getObject(i + 1));
                }
                objects.add(object);
            }
            return objects;
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public boolean delete(T t) {
        try {
            idField.setAccessible(true);
            return deleteById((Integer) idField.get(t));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer integer) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + idFieldName + " = " + integer);
            System.out.println("Executing: " + preparedStatement.toString());
            System.out.println("Deleting " + clazz.getName() + " with id " + integer);
            return preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
