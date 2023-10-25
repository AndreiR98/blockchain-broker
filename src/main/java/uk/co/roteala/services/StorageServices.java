package uk.co.roteala.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.roteala.storage.Storages;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServices {

    @Autowired
    private final Storages storage;
}
