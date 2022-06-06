package cz.encircled.joiner;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Collections;
import java.util.Comparator;

public class TestsOrder implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext context) {
        Collections.sort(context.getClassDescriptors(), new Comparator<ClassDescriptor>() {
            @Override
            public int compare(ClassDescriptor o1, ClassDescriptor o2) {
//                return o1.getDisplayName().startsWith("Eclipse") ? 1 : -1;
                return o1.getDisplayName().startsWith("Eclipse") ? -1 : 1;
            }
        });
    }

}
