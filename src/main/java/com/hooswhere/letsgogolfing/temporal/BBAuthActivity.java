package com.hooswhere.letsgogolfing.temporal;

import com.hooswhere.letsgogolfing.dto.AuthTokens;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface BBAuthActivity {
    AuthTokens authenticate() throws Exception;
}
