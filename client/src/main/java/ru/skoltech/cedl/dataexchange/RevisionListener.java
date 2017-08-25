/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange;

import org.hibernate.HibernateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import ru.skoltech.cedl.dataexchange.entity.Revision;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Special hibernate event listener, which populate {@link Revision} fields
 * of flushed entities with {@link Revision} annotation with current revision number.
 *
 * Created by Nikolay Groshkov on 25-Aug-17.
 */
public class RevisionListener implements FlushEntityEventListener {

    @Override
    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
        Object entity = event.getEntity();

        if (entity == null) {
            return;
        }

        List<Field> revisionFields = findRevisionFields(entity.getClass());
        if (revisionFields.isEmpty()) {
            return;
        }
        if (revisionFields.size() > 2) {
            throw new HibernateException("There must be only one field annotated with @Revision");
        }

        Field revisionField = revisionFields.get(0);
        if (!revisionField.getType().equals(int.class) && !revisionField.getType().equals(Integer.class)) {
            throw new HibernateException("Revision field must be type of int or java.lang.Integer");
        }

        final AuditReader reader = AuditReaderFactory.get(event.getSession());
        DefaultRevisionEntity revisionEntity = reader.getCurrentRevision(CustomRevisionEntity.class, true);
        int revision = revisionEntity.getId();

        try {
            revisionField.setAccessible(true);
            revisionField.setInt(entity, revision);
            revisionField.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new HibernateException("Unable to set revision field", e);
        }
    }

    private static List<Field> findRevisionFields(Class<?> type) {
        return findRevisionFields(new ArrayList<>(), type);
    }

    private static List<Field> findRevisionFields(List<Field> fields, Class<?> type) {
        List<Field> revisionFields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Revision.class)).collect(Collectors.toList());

        fields.addAll(revisionFields);

        if (type.getSuperclass() != null) {
            findRevisionFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
