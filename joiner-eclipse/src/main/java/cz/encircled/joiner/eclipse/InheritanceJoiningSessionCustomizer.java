package cz.encircled.joiner.eclipse;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.Session;

/**
 * @author Vlad on 08-Sep-16.
 */
public class InheritanceJoiningSessionCustomizer implements SessionCustomizer {

    public void customize(Session session) {
        for (ClassDescriptor descriptor : session.getDescriptors().values()) {
            InheritanceJoiningCustomizer customizer = new InheritanceJoiningCustomizer();
            try {
                customizer.customize(descriptor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
