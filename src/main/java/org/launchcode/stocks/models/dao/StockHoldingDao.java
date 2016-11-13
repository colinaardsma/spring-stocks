package org.launchcode.stocks.models.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.launchcode.stocks.models.StockHolding;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Transactional
@Repository
public interface StockHoldingDao extends CrudRepository<StockHolding, Integer> {

    StockHolding findBySymbolAndOwnerId(String symbol, int ownerId);
    
    List<StockHolding> findByOwnerId(int ownerId);

}
