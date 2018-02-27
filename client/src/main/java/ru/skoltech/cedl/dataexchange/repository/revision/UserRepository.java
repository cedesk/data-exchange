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

package ru.skoltech.cedl.dataexchange.repository.revision;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.repository.custom.JpaRevisionEntityRepository;

/**
 * Data Access Operations with {@link User} entity.
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public interface UserRepository extends JpaRevisionEntityRepository<User, Long> {

    /**
     * Retrieve a {@link User} by name.
     *
     * @param userName name of the {@link User}
     * @return instance of the {@link User}
     */
    User findByUserName(String userName);

    /**
     * Determine the existence of the {@link User} with specified name.
     *
     * @param userName user name to search for
     * @return <i>true</i> if user with specified name are exists, <i>false<i/> if opposite
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.userName = :userName")
    boolean existsByName(@Param("userName") String userName);
}
