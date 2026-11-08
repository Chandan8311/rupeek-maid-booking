package com.rupeek.maidbooking.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.rupeek.maidbooking.model.maid.Maid;
public interface MaidRepository {
    Maid save(Maid maid);
    Optional<Maid> findById(UUID id);
    List<Maid> findAll();
    <T> T withMaidLock(UUID maidId, Supplier<T> operation);
}
