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
package conditionalbeans.provider.dummybeans.credentialsservice;

import conditionalbeans.provider.dummybeans.DummyBean;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;
import org.springframework.stereotype.Component;
import de.hsesslingen.keim.efs.middleware.provider.ITokensService;

/**
 *
 * @author keim
 */
@Component
public class DummyCredentialsService extends DummyBean implements ITokensService<AbstractCredentials> {

    @Override
    public TokenCredentials createToken(String userId, String secret) {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    public void deleteToken(String token) {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    public boolean isTokenValid(String token) {
        throw new UnsupportedOperationException("This should never be called.");
    }

}
