package cz.encircled.joiner.eclipse;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.ObjectBuildingQuery;

/**
 * @author Vlad on 13-Sep-16.
 */
public class FixedJoinerAttributeManager extends JoinedAttributeManager {

    public FixedJoinerAttributeManager(ClassDescriptor descriptor, ExpressionBuilder baseBuilder, ObjectBuildingQuery baseQuery) {
        super(descriptor, baseBuilder, baseQuery);
    }

    @Override
    protected void processDataResults(AbstractSession session) {
        int originalMax = baseQuery.getMaxRows();
        int originalFirst = baseQuery.getFirstResult();

        try {
            baseQuery.setMaxRows(0);
            baseQuery.setFirstResult(0);
            super.processDataResults(session);
        } finally {
            baseQuery.setMaxRows(originalMax);
            baseQuery.setFirstResult(originalFirst);
        }

    }
}
