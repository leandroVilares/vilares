package com.ml.logistica.romaneio.dao;

import java.io.IOException;

/**
 * Created by saulo on 08/09/2016.
 */
public class DaoException extends RuntimeException {
    public DaoException(IOException e) {
        super(e);
    }
}
