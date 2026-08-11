package com.bloomframe.server.storage;

public class NoopStorageService implements StorageService {

    @Override
    public boolean isConfigured() {
        return false;
    }
}
