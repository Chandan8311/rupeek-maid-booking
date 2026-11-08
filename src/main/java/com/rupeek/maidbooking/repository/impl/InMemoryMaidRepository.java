package com.rupeek.maidbooking.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Repository;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.repository.MaidRepository;


@Repository
public final class InMemoryMaidRepository implements MaidRepository {
    private final ConcurrentHashMap<UUID, Maid> maids = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public Maid save(Maid maid) {
        maids.put(maid.getId(), maid);
        locks.putIfAbsent(maid.getId(), new ReentrantLock());
        return maid;
    }

    @Override
    public Optional<Maid> findById(UUID maidId) {
        return Optional.ofNullable(maids.get(maidId));
    }

    @Override
    public List<Maid> findAll() {
        return List.copyOf(maids.values());
    }

    @Override
    public <T> T withMaidLock(UUID maidId, Supplier<T> operation) {
        ReentrantLock lock = locks.get(maidId);
        if (lock == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Maid not found: " + maidId);
        }
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }
}
