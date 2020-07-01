/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.consumer;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author ben
 */
@Data
@Accessors(chain = true)
public class ConsumerCredentialsCollection {

    public ConsumerCredentialsCollection() {
    }

    public ConsumerCredentialsCollection(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    private Map<String, String> credentials;

    public String ofService(String id) {
        return credentials.get(id);
    }

    public static ConsumerCredentialsCollection empty() {
        return new ConsumerCredentialsCollection().setCredentials(new HashMap<>());
    }

}
