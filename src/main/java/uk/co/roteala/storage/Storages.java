package uk.co.roteala.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.roteala.common.storage.*;
import uk.co.roteala.configs.BrokerConfigs;
import uk.co.roteala.exceptions.StorageException;
import uk.co.roteala.exceptions.errorcodes.StorageErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Return all blockchain RocksDB storages
 * */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Storages  {

    private final BrokerConfigs configs;
    /**
     * Represents the storage for the state trie.
     */
    private Storage stateTrieStorage;
    /**
     * Represents the storage for the mempool blockchain.
     */
    private Storage mempoolBlockchain;
    /**
     * Represents the storage for the state blockchain.
     */
    private Storage stateBlockchain;
    /**
     * Represents the storage for peers.
     */
    private Storage peers;

    /**
     * Retrieves the appropriate storage instance based on the provided {@link StorageTypes}.
     *
     * @param storageType The type of storage required.
     * @return The storage instance corresponding to the provided type.
     * @throws IllegalArgumentException if an unsupported StorageType is provided.
     */
    public Storage getStorage(StorageTypes storageType) {
        switch (storageType) {
            case STATE:
                return initializeStateTrieStorage();
            case PEERS:
                return initializePeersStorage();
            case MEMPOOL:
                return initializeMempoolStorage();
            case BLOCKCHAIN:
                return initializeBlockchainStorage();
            default:
                throw new IllegalArgumentException("Unsupported StorageType: " + storageType);
        }
    }
    /**
     * Initializes the state trie storage. This method ensures that the necessary path exists,
     * sets up the database options, column family options, and descriptors, and then initializes
     * the state trie storage.
     *
     * @throws StorageException if there is any failure during storage initialization.
     */
    @Bean
    public Storage initializeStateTrieStorage() {
        try {
            DefaultStorage.ensurePathExists(configs.getStateTriePath());

            DBOptions dbOptions = DefaultStorage.setupDatabaseOptions(true);

            ColumnFamilyOptions columnFamilyOptions = DefaultStorage.setupColumnFamilyOptions();

            List<String> stateTrieColumns = new ArrayList<>();
            stateTrieColumns.add(ColumnFamilyTypes.STATE.getName());
            stateTrieColumns.add(ColumnFamilyTypes.ACCOUNTS.getName());
            stateTrieColumns.add(ColumnFamilyTypes.NODE.getName());

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = DefaultStorage
                    .setupColumnFamilyDescriptors(columnFamilyOptions, stateTrieColumns);

            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

            return new Storage(StorageTypes.STATE,
                    RocksDB.open(dbOptions, configs.getStateTriePath().getAbsolutePath(),
                            columnFamilyDescriptors, columnFamilyHandles), columnFamilyHandles);
        } catch (Exception e) {
            log.error("Failed to initialize storage!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
    }

    /**
     * Initializes the storage for mempool data.
     * This method ensures the path exists, sets up the database options,
     * column family options, and column family descriptors, and creates a new Storage instance.
     *
     * @throws StorageException if any error occurs during the initialization.
     */
    @Bean
    public Storage initializeMempoolStorage() {
        try {
            DefaultStorage.ensurePathExists(configs.getMempoolPath());

            DBOptions dbOptions = DefaultStorage.setupDatabaseOptions(true);

            ColumnFamilyOptions columnFamilyOptions = DefaultStorage.setupColumnFamilyOptions();

            List<String> mempoolColumns = new ArrayList<>();
            mempoolColumns.add(ColumnFamilyTypes.TRANSACTIONS.getName());
            mempoolColumns.add(ColumnFamilyTypes.BLOCKS.getName());

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = DefaultStorage
                    .setupColumnFamilyDescriptors(columnFamilyOptions, mempoolColumns);

            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

            return new Storage(StorageTypes.MEMPOOL,
                    RocksDB.open(dbOptions, configs.getMempoolPath().getAbsolutePath(),
                            columnFamilyDescriptors, columnFamilyHandles), columnFamilyHandles);
        } catch (Exception e) {
            log.error("Failed to initialize storage!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
    }

    /**
     * Initializes the storage for blockchain data.
     * This method ensures the path exists, sets up the database options,
     * column family options, and column family descriptors, and creates a new Storage instance.
     *
     * @throws StorageException if any error occurs during the initialization.
     */
    @Bean
    public Storage initializeBlockchainStorage() {
        try {
            DefaultStorage.ensurePathExists(configs.getBlocksPath());

            DBOptions dbOptions = DefaultStorage.setupDatabaseOptions(true);

            ColumnFamilyOptions columnFamilyOptions = DefaultStorage.setupColumnFamilyOptions();

            List<String> stateBlockchainColumns = new ArrayList<>();
            stateBlockchainColumns.add(ColumnFamilyTypes.TRANSACTIONS.getName());
            stateBlockchainColumns.add(ColumnFamilyTypes.BLOCKS.getName());

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = DefaultStorage
                    .setupColumnFamilyDescriptors(columnFamilyOptions, stateBlockchainColumns);

            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

            return new Storage(StorageTypes.BLOCKCHAIN,
                    RocksDB.open(dbOptions, configs.getBlocksPath().getAbsolutePath(),
                            columnFamilyDescriptors, columnFamilyHandles), columnFamilyHandles);
        } catch (Exception e) {
            log.error("Failed to initialize storage!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
    }

    /**
     * Initializes the storage for peers data.
     * This method ensures the path exists, sets up the database options,
     * and creates a new Storage instance without any specific column families.
     *
     * @throws StorageException if any error occurs during the initialization.
     */
    @Bean
    public Storage initializePeersStorage() {
        try {
            DefaultStorage.ensurePathExists(configs.getPeersPath());

            Options options = DefaultStorage.setupOptions(false);

            return new Storage(StorageTypes.PEERS,
                    RocksDB.open(options, configs.getPeersPath().getAbsolutePath()), null);
        } catch (Exception e) {
            log.error("Failed to initialize storage!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
    }
}
