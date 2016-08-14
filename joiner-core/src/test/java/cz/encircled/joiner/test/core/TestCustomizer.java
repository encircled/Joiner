package cz.encircled.joiner.test.core;

import cz.encircled.joiner.test.model.NormalUser;
import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.descriptors.InstanceVariableAttributeAccessor;
import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * @author Vlad on 13-Aug-16.
 */
public class TestCustomizer implements DescriptorCustomizer {

    @Override
    public void customize(ClassDescriptor descriptor) throws Exception {
        if (descriptor.getJavaClass().equals(NormalUser.class)) {
            DatabaseMapping passwords = descriptor.getMappingForAttributeName("passwords");
            passwords.setAttributeAccessor(new My());
        }
    }

    public static class My extends InstanceVariableAttributeAccessor {

        @Override
        public void setAttributeValueInObject(Object anObject, Object value) {
            /*IndirectSet<?> indirectSet = (IndirectSet<?>) value;
            if(indirectSet.getValueHolder() instanceof UnitOfWorkQueryValueHolder) {
                UnitOfWorkQueryValueHolder vh = (UnitOfWorkQueryValueHolder) indirectSet.getValueHolder();
                vh.getRow();
                if(vh.isEasilyInstantiated()) {
                    System.out.println();
                }
            }*/
            super.setAttributeValueInObject(anObject, value);
        }


    }

}
