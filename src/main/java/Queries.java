import java.util.List;

public interface Queries<T,ID> {

    boolean insert(T t);

    T selectById(ID id);

    List<T> selectAll();

    boolean delete(T t);

    boolean deleteById(ID id);
}
