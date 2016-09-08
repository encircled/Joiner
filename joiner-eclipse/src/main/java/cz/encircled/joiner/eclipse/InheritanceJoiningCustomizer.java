package cz.encircled.joiner.eclipse;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.InheritancePolicy;
import org.eclipse.persistence.internal.descriptors.ObjectBuilder;
import org.eclipse.persistence.mappings.DatabaseMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This customizer fixes fetch joining of nested associations, which are present on a subclass only
 *
 * @author Vlad on 06-Sep-16.
 */
public class InheritanceJoiningCustomizer implements DescriptorCustomizer {

    private static final String SPACE = " ";

    @Override
    public void customize(ClassDescriptor descriptor) throws Exception {
        try {
            Method setObjectBuilder = descriptor.getClass().getSuperclass().getDeclaredMethod("setObjectBuilder", ObjectBuilder.class);
            setObjectBuilder.setAccessible(true);
            setObjectBuilder.invoke(descriptor, new InheritanceObjectBuilder(descriptor, descriptor.getObjectBuilder()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Warning: never call `descriptor.getInheritancePolicy()` if an entity has no subclasses,
     * otherwise it create new InheritancePolicy, which causes NPE at InheritancePolicy#classFromRow(...)
     */
    private class InheritanceObjectBuilder extends ObjectBuilder {

        InheritanceObjectBuilder(ClassDescriptor descriptor, ObjectBuilder original) {
            super(descriptor);

            this.setMappingsByAttribute(new HashMap<>(get(original, "mappingsByAttribute")));
            this.setMappingsByField(new HashMap<>(original.getMappingsByField()));
            this.setFieldsMap(new HashMap<>(original.getFieldsMap()));
            this.setReadOnlyMappingsByField(new HashMap<>(original.getReadOnlyMappingsByField()));
            this.setPrimaryKeyMappings(new ArrayList<>(original.getPrimaryKeyMappings()));
            if (nonPrimaryKeyMappings != null) {
                this.setNonPrimaryKeyMappings(new ArrayList<>(get(original, "nonPrimaryKeyMappings")));
            }
            this.cloningMappings = new ArrayList<>(original.getCloningMappings());
            this.eagerMappings = new ArrayList<>(original.getEagerMappings());
            this.relationshipMappings = new ArrayList<>(original.getRelationshipMappings());
        }

        @Override
        public DatabaseMapping getMappingForAttributeName(String name) {
            DatabaseMapping mapping = super.getMappingForAttributeName(name);
            // name contains spaces for "treat as ..." entries, just skip it
            if (mapping == null && !name.contains(SPACE)) {
                InheritancePolicy policy = this.descriptor.getInheritancePolicyOrNull();
                if (policy != null) {
                    for (ClassDescriptor child : policy.getChildDescriptors()) {
                        DatabaseMapping childMapping = child.getObjectBuilder().getMappingForAttributeName(name);
                        if (childMapping != null) {
                            return childMapping;
                        }
                    }
                }
            }

            return mapping;
        }

        @SuppressWarnings("unchecked")
        private <T> T get(ObjectBuilder original, String name) {
            try {
                Field field = original.getClass().getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return (T) field.get(original);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}
