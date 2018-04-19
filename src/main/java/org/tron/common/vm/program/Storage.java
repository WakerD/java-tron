/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tron.common.vm.program;

import org.tron.common.vm.DataWord;
import org.tron.common.vm.program.invoke.ProgramInvoke;
import org.tron.common.vm.program.listener.ProgramListener;
import org.tron.common.vm.program.listener.ProgramListenerAware;
import org.tron.core.db.Repository;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.db.ByteArrayWrapper;
import org.tron.core.db.ContractDetails;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Storage implements Repository, ProgramListenerAware {

    private final Repository repository;
    private final DataWord address;
    private ProgramListener programListener;

    public Storage(ProgramInvoke programInvoke) {
        this.address = programInvoke.getOwnerAddress();
        this.repository = programInvoke.getRepository();
    }

    @Override
    public void setProgramListener(ProgramListener listener) {
        this.programListener = listener;
    }

    @Override
    public AccountCapsule createAccount(byte[] addr, Protocol.AccountType type) {
        return repository.createAccount(addr, type);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return repository.isExist(addr);
    }

    @Override
    public AccountCapsule getAccountCapsule(byte[] addr) {
        return repository.getAccountCapsule(addr);
    }

    @Override
    public void delete(byte[] addr) {
        if (canListenTrace(addr)) programListener.onStorageClear();
        repository.delete(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return repository.getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return repository.hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        repository.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return repository.getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return repository.getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        if (canListenTrace(addr)) programListener.onStoragePut(key, value);
        repository.addStorageRow(addr, key, value);
    }

    private boolean canListenTrace(byte[] address) {
        return (programListener != null) && this.address.equals(new DataWord(address));
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return repository.getStorageValue(addr, key);
    }

    @Override
    public long getBalance(byte[] addr) {
        return repository.getBalance(addr);
    }

    @Override
    public long addBalance(byte[] addr, long value) {
        return repository.addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return repository.getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        repository.dumpState(block, gasUsed, txNumber, txHash);
    }

    //@Override
    //public Repository startTracking() {
    //    return repository.startTracking();
    //}

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public void flushNoReconnect() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void commit() {
        repository.commit();
    }

    @Override
    public void rollback() {
        repository.rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        repository.syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return repository.isClosed();
    }

    @Override
    public void close() {
        repository.close();
    }

    @Override
    public void reset() {
        repository.reset();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountCapsule> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetails) {
        for (ByteArrayWrapper address : contractDetails.keySet()) {
            if (!canListenTrace(address.getData())) return;

            ContractDetails details = contractDetails.get(address);
            if (details.isDeleted()) {
                programListener.onStorageClear();
            } else if (details.isDirty()) {
                for (Map.Entry<DataWord, DataWord> entry : details.getStorage().entrySet()) {
                    programListener.onStoragePut(entry.getKey(), entry.getValue());
                }
            }
        }
        repository.updateBatch(accountStates, contractDetails);
    }

    @Override
    public byte[] getRoot() {
        return repository.getRoot();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountCapsule> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        repository.loadAccount(addr, cacheAccounts, cacheDetails);
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStorageSize(byte[] addr) {
        return repository.getStorageSize(addr);
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return repository.getStorageKeys(addr);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return repository.getStorage(addr, keys);
    }
}