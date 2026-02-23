package cz.encircled.joiner;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class TestsOrder implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors().sort(Comparator.comparing(
                (ClassDescriptor cd) -> !cd.getDisplayName().startsWith("Eclipse")
        ));
    }

}
