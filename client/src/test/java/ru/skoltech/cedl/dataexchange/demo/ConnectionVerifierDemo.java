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

package ru.skoltech.cedl.dataexchange.demo;

import ru.skoltech.cedl.dataexchange.repository.ConnectionVerifier;

public class ConnectionVerifierDemo {

    public static void main(String... args) {
        if (args.length < 2) {
            System.out.println("usage: ConnectionVerifierDemo <ip|hostname> port");
            System.exit(-1);
        }
        String ip = args[0];
        System.out.println("pinging " + ip);
        boolean isReachable = ConnectionVerifier.isServerReachable(ip, 500);
        if (isReachable) {
            System.out.println("SUCCESSFULL");
        } else {
            System.out.println("FAILED");
        }

        int port = Integer.valueOf(args[1]);
        System.out.println("checking port " + port);
        boolean portIsOpen = ConnectionVerifier.isServerListening(ip, port, 500);
        if (portIsOpen) {
            System.out.println("SUCCESSFULL");
        } else {
            System.out.println("FAILED");
        }
    }


}
