package cz.encircled.joiner.ksp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.List;
import java.util.Map;

@Entity
public class JavaCustomer {

    @Column
    Integer intValue;

    @Column
    List<Object> listOfObjects;

    @Column
    String[] arrayOfStrings;

    @Column
    Map<String, Integer> mapStrToInt;

}
