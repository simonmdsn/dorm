import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            Connection postgres = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "123");
            Person person = new Person("John", "Doe");
            Dorm<Person> personRepository = new Dorm<>(postgres, Person.class);
            personRepository.insert(person);
            List<Person> people = personRepository.selectAll();
            System.out.println(people);
            Person person1 = personRepository.selectById(1);
            System.out.println(person1);
            personRepository.delete(person1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
