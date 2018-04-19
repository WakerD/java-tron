package org.tron.core.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.core.capsule.ContractCapsule;

/**
 *
 * Created by Guo Yonggang on 04.14.2018
 */
@Slf4j
public class ContractStore extends TronStoreWithRevoking<ContractCapsule> {

  private ContractStore(String dbName) {
    super(dbName);
  }

  @Override
  public ContractCapsule get(byte[] key) {
    byte[] value = dbSource.getData(key);
    return ArrayUtils.isEmpty(value) ? null : new ContractCapsule(value);
  }


  @Override
  public boolean has(byte[] key) {
    byte[] contract = dbSource.getData(key);
    return null != contract;
  }

  /**
   * get total transaction.
   */
  public long getTotalContracts() {
    return dbSource.getTotal();
  }

  private static ContractStore instance;

  public static void destory() {
    instance = null;
  }

  void destroy() {
    instance = null;
  }

  /**
   * create Fun.
   */
  public static ContractStore create(String dbName) {
    if (instance == null) {
      synchronized (ContractStore.class) {
        if (instance == null) {
          instance = new ContractStore(dbName);
        }
      }
    }
    return instance;
  }

  /**
   * find a transaction  by it's id.
   */
  public byte[] findContractByHash(byte[] trxHash) {
    return dbSource.getData(trxHash);
  }

}