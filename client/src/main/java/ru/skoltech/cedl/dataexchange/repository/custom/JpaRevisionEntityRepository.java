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

package ru.skoltech.cedl.dataexchange.repository.custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * Extension of {@link JpaRepository} which aimed at store additional revision information
 * along with save/delete changes.
 * Convenient repository for any {@link org.hibernate.envers.Audited} entity.
 *
 * Created by Nikolay Groshkov on 10-Aug-17.
 */
@NoRepositoryBean
public interface JpaRevisionEntityRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    @Transactional
    @Override
    <S extends T> S save(S entity);

    @Transactional
    @Override
    <S extends T> List<S> save(Iterable<S> entities);

    @Transactional
    @Override
    <S extends T> S saveAndFlush(S entity);

    @Transactional
    @Override
    void delete(ID id);

    @Transactional
    @Override
    void delete(T entity);

    @Transactional
    @Override
    void delete(Iterable<? extends T> entities);

    @Transactional
    @Override
    void deleteAll();

    @Transactional
    @Override
    void deleteInBatch(Iterable<T> entities);

    @Transactional
    @Override
    void deleteAllInBatch();

}