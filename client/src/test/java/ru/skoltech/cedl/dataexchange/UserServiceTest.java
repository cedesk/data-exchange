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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.UserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by dknoll on 09.06.2015
 */
public class UserServiceTest extends AbstractApplicationContextTest {

    private UserService userService;

    @Before
    public void prepare() {
        userService = context.getBean(UserService.class);
    }

    @Test
    public void testFindAdminUser() {
        User adminUser = userService.findAdminUser();
        assertNotNull(adminUser);
        assertEquals(userService.ADMIN_USER_NAME, adminUser.getUserName());
    }
}
