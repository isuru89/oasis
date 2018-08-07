package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IOasisApiService;

/**
 * @author iweerarathna
 */
public class AuthService extends BaseService {
    AuthService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);
    }


}
