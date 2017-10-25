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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializer;

import static ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer.BASE_CONTEXT_LOCATION;
import static ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer.SERVICE_CONTEXT_LOCATION;

/**
 * Create an empty scheme.
 *
 * Created by Nikolay Groshkov on 17-Oct-17.
 */
public class CreateEmptyScheme {

    public static void main(String[] args) {
        ApplicationSettingsInitializer.initialize();
        ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(SERVICE_CONTEXT_LOCATION, BASE_CONTEXT_LOCATION);
        context.close();
    }
}
